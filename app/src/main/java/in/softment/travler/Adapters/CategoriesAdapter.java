package in.softment.travler.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import in.softment.travler.AllVideosViewController;
import in.softment.travler.Model.Category;
import in.softment.travler.Model.Video;
import in.softment.travler.PdfViewActivity;
import in.softment.travler.R;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Category> categories;

    public CategoriesAdapter(Context context, ArrayList<Category> categories){
        this.context = context;
        this.categories = categories;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.workout_home_view, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
           holder.setIsRecyclable(false);
           Category category = categories.get(position);
           holder.title.setText(category.title);
           holder.description.setText(category.desc);
           holder.totalAvailable.setText("Available : "+category.totalVideos);
           Glide.with(context).load(category.image).placeholder(R.drawable.logo).into(holder.imageView);

           holder.view.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if (category.type.equalsIgnoreCase("pdf")) {
                       Intent intent = new Intent(context, PdfViewActivity.class);
                       intent.putExtra("cat_id",category.id);
                       context.startActivity(intent);
                   }
                   else {
                       Intent intent = new Intent(context, AllVideosViewController.class);
                       intent.putExtra("cat_id",category.id);
                       intent.putExtra("title",category.title);
                       context.startActivity(intent);
                   }

               }
           });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, description, totalAvailable;
        private ImageView imageView;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mTitle);
            description = itemView.findViewById(R.id.mDescription);
            totalAvailable = itemView.findViewById(R.id.mAvailable);
            imageView = itemView.findViewById(R.id.mImage);



            view = itemView;

        }
    }


}
