package com.frsarker.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.frsarker.newsapp.ArticleActivity;
import com.frsarker.newsapp.Config;
import com.frsarker.newsapp.DBHelper;
import com.frsarker.newsapp.Extras;
import com.frsarker.newsapp.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {
    Context context;
    DBHelper mydb;
    ArrayList<HashMap<String, String>> dataList;
    public CarouselAdapter(Context context,  DBHelper db, ArrayList<HashMap<String, String>> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.mydb = db;
    }
    public static class CarouselViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView itemContainer;
        public ImageButton menu;
        public TextView rowTitle, rowDate, rowCategoryName;
        public ImageView rowThumbnail;
        public CarouselViewHolder(View view) {
            super(view);
            this.itemContainer = view.findViewById(R.id.itemContainer);
            this.menu = view.findViewById(R.id.menu);
            this.rowTitle = view.findViewById(R.id.row_title);
            this.rowDate = view.findViewById(R.id.row_date);
            this.rowCategoryName = view.findViewById(R.id.row_category_name);
            this.rowThumbnail = view.findViewById(R.id.row_thumbnail);
        }
    }
    public CarouselViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_carousel, parent, false);
        CarouselViewHolder viewHolder = new CarouselViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselViewHolder holder, int position) {
        final HashMap<String, String> singleTask = dataList.get(position);

        holder.rowTitle.setText(singleTask.get("post_title"));
        holder.rowDate.setText(singleTask.get("post_date"));
        holder.rowCategoryName.setText(singleTask.get("cat_name"));

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = Extras.popupMenu(context, mydb, singleTask.get("post_id"), v);
                popup.show();
            }
        });

        try {
            Picasso.get()
                    .load(Config.host+"/styles/files/thumbs/"+singleTask.get("post_cover"))
                    .resize(300, 200)
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .into(holder.rowThumbnail);
        }catch (Exception e){}

        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, ArticleActivity.class);
                myIntent.putExtra("post_id", singleTask.get("post_id"));
                context.startActivity(myIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
