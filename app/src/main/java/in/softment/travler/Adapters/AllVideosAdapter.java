package in.softment.travler.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.softment.travler.Model.Video;
import in.softment.travler.OpenPdfActivity;
import in.softment.travler.PlayVideoActivity;
import in.softment.travler.R;
import in.softment.travler.Utils.Services;

public class AllVideosAdapter extends RecyclerView.Adapter<AllVideosAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Video> videos;
    private boolean hasMembership = false;
    private String cat_id = "";

    public AllVideosAdapter(Context context, ArrayList<Video> videos, String cat_id,boolean hasMembership){
        this.context = context;
        this.videos = videos;
        this.hasMembership = hasMembership;
        this.cat_id = cat_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.videos_layout_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Video video = videos.get(position);
        holder.title.setText(video.getTitle());
        holder.type.setText(video.type);
        holder.duration.setText(video.duration);
        holder.checkBox.setChecked(false);
        if (hasMembership) {
             holder.checkBox.setVisibility(View.VISIBLE);
             holder.lock.setVisibility(View.GONE);

             //ischecked
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Watched").document(video.id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        holder.checkBox.setChecked(true);
                    }
                }
            });
        }
        else {
            holder.checkBox.setVisibility(View.GONE);
            holder.lock.setVisibility(View.VISIBLE);
        }

        if (video.type.equalsIgnoreCase("pdf")) {
            holder.typeImage.setVisibility(View.VISIBLE);
            holder.durationView.setVisibility(View.GONE);
            holder.play.setVisibility(View.GONE);
            holder.download.setVisibility(View.VISIBLE);
            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasMembership) {
                        Intent intent = new Intent(context, OpenPdfActivity.class);
                        intent.putExtra("pdfLink",video.pdfLink);
                        intent.putExtra("title",video.title);
                        context.startActivity(intent);
                    }
                    else {
                        Services.showDialog(context,"Required Premium Membership","Please purchase premium membership and unlock all contents.");
                    }

                }
            });
        }
        else {
            holder.play.setVisibility(View.VISIBLE);
            holder.download.setVisibility(View.GONE);
            holder.duration.setText(video.duration);
            holder.typeView.setVisibility(View.GONE);
            holder.durationView.setVisibility(View.VISIBLE);

            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasMembership) {
                        Intent intent = new Intent(context, PlayVideoActivity.class);
                        intent.putExtra("videourl",video.videoLink);
                        context.startActivity(intent);
                    }
                    else {
                        Services.showDialog(context,"Required Premium Membership","Please purchase premium membership and unlock all contents.");
                    }

                }
            });

        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                WriteBatch writeBatch =  FirebaseFirestore.getInstance().batch();
                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Map<String, Object> map = new HashMap();
                if (b) {
                    map.put("count", FieldValue.increment(1));
                 writeBatch.set(documentReference.collection("WatchedCount").document(cat_id),map, SetOptions.merge());

                    Map<String, String> map1 = new HashMap();
                    map1.put("videoId", video.id);
                 writeBatch.set(documentReference.collection("Watched").document(video.id),map1);

                }
                else {
                    map.put("count", FieldValue.increment(-1));
                    writeBatch.set(documentReference.collection("WatchedCount").document(cat_id),map, SetOptions.merge());
                    writeBatch.delete(documentReference.collection("Watched").document(video.id));
                }
                writeBatch.commit();
            }
        });






    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title,duration, type;
        private ImageView lock, download,play, typeImage;
        private CheckBox checkBox;
        private LinearLayout durationView, typeView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            title = itemView.findViewById(R.id.mTitle);
            duration = itemView.findViewById(R.id.time);
            type = itemView.findViewById(R.id.type);
            lock =itemView.findViewById(R.id.lock);
            download = itemView.findViewById(R.id.download);
            play = itemView.findViewById(R.id.play);
            checkBox = itemView.findViewById(R.id.checkBox);
            typeImage = itemView.findViewById(R.id.typeImage);
            durationView = itemView.findViewById(R.id.durationView);
            typeView = itemView.findViewById(R.id.typeLL);


        }
    }


}
