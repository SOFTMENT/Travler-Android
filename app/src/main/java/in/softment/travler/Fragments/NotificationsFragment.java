package in.softment.travler.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.softment.travler.Adapters.NotificationAdapter;
import in.softment.travler.Model.NotificationModel;
import in.softment.travler.R;
import in.softment.travler.Utils.ProgressHud;

public class NotificationsFragment extends Fragment {


    private Context context;
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationModel> notificationModels;
    private TextView no_notifications_available;
    public NotificationsFragment(Context context) {
        this.context = context;
    }



    public NotificationsFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        notificationModels = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(context,notificationModels);
        recyclerView.setAdapter(notificationAdapter);
        no_notifications_available = view.findViewById(R.id.no_notifications_available);
        getNotifications();
        return view;
    }


    private void getNotifications() {
        FirebaseFirestore.getInstance().collection("Notifications").orderBy("notificationTime",
                Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    notificationModels.clear();
                    if (task.getResult() != null && !task.getResult().isEmpty()) {

                        for(DocumentSnapshot documentsnap : task.getResult().getDocuments()) {

                            NotificationModel notificationModel = documentsnap.toObject(NotificationModel.class);
                            notificationModels.add(notificationModel);

                        }
                    }
                    if (notificationModels.size() > 0) {
                        no_notifications_available.setVisibility(View.GONE);
                    }
                    else {
                        no_notifications_available.setVisibility(View.VISIBLE);
                    }
                    notificationAdapter.notifyDataSetChanged();
                }

            }
        });
    }
}