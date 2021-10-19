package in.softment.travler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Services;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_sign_in);

        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        TextView versionCode = findViewById(R.id.version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionCode.setText("Version - "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sEmail = email.getText().toString().trim();
                String sPassword = password.getText().toString().trim();

                if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignInActivity.this,"Enter Email Address");
                }
                else if (sPassword.isEmpty()) {
                    Services.showCenterToast(SignInActivity.this,"Enter Password");
                }
                else {
                    ProgressHud.show(SignInActivity.this,"Sign In...");
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()) {
                                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    Services.getCurrentUserData(SignInActivity.this,FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                                }
                                else {
                                    Services.sentEmailVerificationLink(SignInActivity.this);
                                }
                            }
                            else {
                                Services.showDialog(SignInActivity.this,"Error",task.getException().getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sEmail = email.getText().toString().trim();
                if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignInActivity.this,"Enter Email Address");
                }
                else {
                    ProgressHud.show(SignInActivity.this,"");
                    FirebaseAuth.getInstance().sendPasswordResetEmail(sEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()){
                                Services.showDialog(SignInActivity.this,"Password Reset","We have sent password reset link to your mail address.");
                            }
                            else {
                                Services.showDialog(SignInActivity.this,"Error",task.getException().getLocalizedMessage());
                            }
                        }
                    });
                }

            }
        });

        findViewById(R.id.apple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OAuthProvider.Builder provider = OAuthProvider.newBuilder("apple.com");

                List<String> scopes =
                        new ArrayList<String>() {
                            {
                                add("email");
                                add("name");
                            }
                        };
                provider.setScopes(scopes);


                Task<AuthResult> pending = mAuth.getPendingAuthResult();
                if (pending != null) {
                    pending.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                               appleSignIn(provider);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("APPLE_ERROR",e.getLocalizedMessage());
                        }
                    });
                }
                else {
                    appleSignIn(provider);
                }





            }
        });



        findViewById(R.id.createNewAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });



    }

    public void appleSignIn(OAuthProvider.Builder provider){
        mAuth.startActivityForSignInWithProvider(SignInActivity.this, provider.build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                ProgressHud.show(SignInActivity.this,"");
                                FirebaseFirestore.getInstance().collection("Users").document(authResult.getUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        ProgressHud.dialog.dismiss();
                                        if (task.isSuccessful()) {
                                            if (task.getResult() != null && task.getResult().exists()) {
                                                Services.getCurrentUserData(SignInActivity.this, authResult.getUser().getUid(),true);
                                            }
                                            else {
                                                String profilePic = "https://firebasestorage.googleapis.com/v0/b/travlr-61577.appspot.com/o/png-4.png?alt=media&token=a2dbdc8f-747f-4045-907f-4dfa200cacfe";
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("uid" ,FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                map.put("profilePic",profilePic);
                                                map.put("email","support@travler.com");
                                                map.put("name", "Travler");
                                                map.put("hasMembership",false);
                                                map.put("registredAt",FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp());
                                                map.put("regiType","apple");
                                                Services.addUserDataOnServer(SignInActivity.this, authResult.getUser().getUid(),"apple",map);
                                            }
                                        }

                                    }
                                });



                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("APPLE_ERROR",e.getLocalizedMessage());
                            }
                        });
    }


}