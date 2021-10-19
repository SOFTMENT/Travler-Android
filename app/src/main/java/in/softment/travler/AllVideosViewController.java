package in.softment.travler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.softment.travler.Adapters.AllVideosAdapter;
import in.softment.travler.Model.UserModel;
import in.softment.travler.Model.Video;
import in.softment.travler.Utils.Constants;
import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Services;

public class AllVideosViewController extends AppCompatActivity {
    private ArrayList<Video> videos;
    private RecyclerView recyclerView;
    private AllVideosAdapter allVideosAdapter;
    public static final String SUBSCRIBE_KEY= "subscribe";
    public static final String PREF_FILE= "MyPref";
    private boolean hasMembership = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_videos_view_controller);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String cat_id = getIntent().getStringExtra("cat_id");
        String title = getIntent().getStringExtra("title");
        if (getSubscribeValueFromPref() || (UserModel.data.expireDate.compareTo(Constants.currentDate) > 0)){
            hasMembership = true;
        }
        TextView headTitle = findViewById(R.id.title);
        headTitle.setText(title);
        videos = new ArrayList<>();
        allVideosAdapter = new AllVideosAdapter(this,videos, cat_id,hasMembership);
        recyclerView.setAdapter(allVideosAdapter);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        getVideos(cat_id);
    }

    private boolean getSubscribeValueFromPref(){
        return getPreferenceObject().getBoolean( SUBSCRIBE_KEY,false);
    }

    private SharedPreferences getPreferenceObject() {
        return getSharedPreferences(PREF_FILE, 0);
    }

    public void getVideos(String cat_id){
        ProgressHud.show(this,"Loading...");
        FirebaseFirestore.getInstance().collection("Categories").document(cat_id).collection("Videos").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    ProgressHud.dialog.dismiss();
                    if (error == null) {
                        videos.clear();
                        if (value != null && !value.isEmpty()) {
                           for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                               Video video = documentSnapshot.toObject(Video.class);
                               videos.add(video);
                               Log.d("SOFTMENTVIJAY",video.id);
                           }
                        }
                        allVideosAdapter.notifyDataSetChanged();
                    }
                    else {
                        Services.showDialog(AllVideosViewController.this,"Error",error.getLocalizedMessage());
                    }
            }
        });

    }
}