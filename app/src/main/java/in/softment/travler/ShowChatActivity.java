package in.softment.travler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import in.softment.travler.Adapters.LiveChatAdapter;
import in.softment.travler.Model.ChatModel;
import in.softment.travler.Model.UserModel;
import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Services;


public class ShowChatActivity extends AppCompatActivity {
    private int PICK_FILE_REQUEST = 909;
    private Uri downloadUri;
    private LiveChatAdapter liveChatAdapter;
    private FirebaseAuth firebaseAuth;
    private String uid;
    private List<ChatModel> chatModels;
    private EditText editText;
    RecyclerView recyclerView;
    private  String title,isAdmin,hasVideoAccess;
    private LinearLayoutManager linearLayoutManager;
    private RelativeLayout chatlayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_main, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_main));
        }

        ((ImageView)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chatModels = new ArrayList<>();

        final ImageView sent = findViewById(R.id.sent);
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        editText = findViewById(R.id.message);
        sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sMessage = editText.getText().toString().trim();

                if (!sMessage.isEmpty()) {
                    editText.setText("");
                    sentMessage(sMessage, uid);

                }
                else {

                    editText.requestFocus();
                    editText.setError("Empty");

                }
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setHasFixedSize(true);
        liveChatAdapter = new LiveChatAdapter(this,chatModels,uid);
        recyclerView.setAdapter(liveChatAdapter);

        ImageView attachment = findViewById(R.id.attachment);
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();

            }

        });
        getdetails();
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 605);
                return false;
            }else {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 605) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFileChooser();
            } else {

                Toast toast = Toast.makeText(ShowChatActivity.this, "Permission Denied.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }
        }
    }

    public void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_FILE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && data != null && data.getData() != null) {
            downloadUri = data.getData();
            uploadDocOnFirebase();


        }
    }

    private void uploadDocOnFirebase() {
        ProgressHud.show(this,"Wait...");
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ChatImages").child(UUID.randomUUID().toString());
        UploadTask uploadTask = storageReference.putFile(downloadUri);
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    ProgressHud.dialog.dismiss();
                    throw  task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    String downloadFileUri = task.getResult().toString();
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("message",downloadFileUri);
                    hashMap.put("sender",uid);
                    hashMap.put("type","image");
                    String messageId = FirebaseFirestore.getInstance().collection("Chats").document().getId();
                    hashMap.put("id",messageId);
                    hashMap.put("profileImage", UserModel.data.profilePic);
                    hashMap.put("date",new Date());
                    String name = UserModel.data.getName();
                    String[] names = name.split(" ");
                    name = "";
                    for (int i = 0; i< names.length ; i++){
                        name += names[i].substring(0,1).toUpperCase() + names[i].substring(1).toLowerCase() +" ";
                    }
                    hashMap.put("name",name.trim());
                    FirebaseFirestore.getInstance().collection("Chats").document(messageId).set(hashMap);

                }
                else{
                    Toast toast = Toast.makeText(ShowChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }


            }
        });
    }

    private void sentMessage(String sMessage, String uid) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("message",sMessage);
        hashMap.put("sender",uid);
        hashMap.put("type","text");
        String messageId = FirebaseFirestore.getInstance().collection("Chats").document().getId();
        hashMap.put("id",messageId);
        hashMap.put("profileImage", UserModel.data.profilePic);
        hashMap.put("date",new Date());
        String name = UserModel.data.getName();
        String[] names = name.split(" ");
        name = "";
        for (int i = 0; i< names.length ; i++){
            name += names[i].substring(0,1).toUpperCase() + names[i].substring(1).toLowerCase() +" ";
        }
        hashMap.put("name",name.trim());
        FirebaseFirestore.getInstance().collection("Chats").document(messageId).set(hashMap);
    }

    private void getdetails() {
        ProgressHud.show(ShowChatActivity.this, "Loading...");
        FirebaseFirestore.getInstance().collection("Chats").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                ProgressHud.dialog.dismiss();
                if (error == null) {
                    chatModels.clear();
                    if (value != null && !value.isEmpty()) {
                       for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                            chatModels.add(chatModel);
                       }
                    }

                    if (chatModels.size() > 0) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.scrollToPosition(chatModels.size()-1);

                            }
                        });
                    }
                    liveChatAdapter.notifyDataSetChanged();

                }
                else {
                    Services.showDialog(ShowChatActivity.this,"Error",error.getMessage());
                }
            }
        });


    }



}
