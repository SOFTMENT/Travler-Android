package in.softment.travler.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.softment.travler.Model.NotificationModel;
import in.softment.travler.R;
import in.softment.travler.Utils.Services;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> notificationModels) {

        this.context = context;
        this.notificationModels = notificationModels;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_notifications_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        NotificationModel notificationModel = notificationModels.get(position);
        holder.title.setText(notificationModel.getTitle());
        holder.message.setText(notificationModel.getMessage());
        holder.time.setText(Services.getTimeAgo(notificationModel.getNotificationTime()));
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, message, time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mTitle);
            message = itemView.findViewById(R.id.mMessage);
            time =  itemView.findViewById(R.id.mTime);
        }
    }
}
