package in.softment.travler.Adapters;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.travler.Model.ChatModel;
import in.softment.travler.R;
import in.softment.travler.Utils.Services;

public class LiveChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int LEFT_MESSAGE = 0;
    private int RIGHT_MESSAGE = 1;
    private List<ChatModel> chatModels;
    private Context context;
    private String isAdmin;
    private String uid = "";

    public LiveChatAdapter(Context  context, List<ChatModel> chatModels, String uid) {
        this.context = context;
        this.chatModels = chatModels;
        this.uid = uid;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == LEFT_MESSAGE) {
            return new MyHolderLeft(LayoutInflater.from(context).inflate(R.layout.left_message, parent, false));
        }
        else {
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.right_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        if (holder.getItemViewType() == LEFT_MESSAGE) {
            MyHolderLeft myHolderLeft  = (MyHolderLeft)holder;
            myHolderLeft.name.setText(chatModels.get(position).getName());
            Glide.with(context).load(chatModels.get(position).getProfileImage()).into(myHolderLeft.imageView);

            if (chatModels.get(position).getDate() != null) {
                myHolderLeft.dateandtime.setText(Services.convertDateToTimeString(chatModels.get(position).getDate()));
            }
            else {
                myHolderLeft.dateandtime.setText("TIME NOT AVAILABLE");
            }

            if(chatModels.get(position).getType().equalsIgnoreCase("text")) {
                myHolderLeft.imagerr.setVisibility(View.GONE);
                myHolderLeft.message.setVisibility(View.VISIBLE);
                myHolderLeft.message.setText(chatModels.get(position).getMessage());

            }

            else {
                myHolderLeft.imagerr.setVisibility(View.VISIBLE);
                myHolderLeft.message.setVisibility(View.GONE);

                Glide.with(context).load(chatModels.get(position).getMessage()).into(myHolderLeft.imageMessage);

            }



        }
        else {
            MyHolder myHolder = (MyHolder)holder;
            if (chatModels.get(position).getDate() != null) {
                    myHolder.datenadtime.setText(Services.convertDateToTimeString(chatModels.get(position).getDate()));
            }
            else {
                myHolder.datenadtime.setText("TIME NOT AVAILABLE");
            }
            if(chatModels.get(position).getType().equalsIgnoreCase("text")) {
                myHolder.imagerr.setVisibility(View.GONE);
                myHolder.message.setVisibility(View.VISIBLE);
                myHolder.message.setText(chatModels.get(position).getMessage());


            }

            else {
                myHolder.imagerr.setVisibility(View.VISIBLE);
                myHolder.message.setVisibility(View.GONE);

                Glide.with(context).load(chatModels.get(position).getMessage()).into(myHolder.imageMessage);
            }





        }


    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private ImageView imageMessage;
        private RelativeLayout imagerr;
        private View view;
        private TextView datenadtime;
        MyHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            message = itemView.findViewById(R.id.message);
            imageMessage = itemView.findViewById(R.id.imagemessage);
            imagerr = itemView.findViewById(R.id.imagerr);
            datenadtime = itemView.findViewById(R.id.dateandtime);




        }
    }

    static class MyHolderLeft extends RecyclerView.ViewHolder {
        private TextView message;
        private CircleImageView imageView;
        private TextView name;
        private ImageView imageMessage;
        private RelativeLayout imagerr;

        private View view;
        private TextView dateandtime;

        MyHolderLeft(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            message = itemView.findViewById(R.id.message);
            imageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            imageMessage = itemView.findViewById(R.id.imagemessage);
            imagerr = itemView.findViewById(R.id.imagerr);

            dateandtime = itemView.findViewById(R.id.dateandtime);


        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatModels.get(position).getSender().equals(uid)) {
            return RIGHT_MESSAGE;
        }
        else {
            return LEFT_MESSAGE;
        }
    }
}
