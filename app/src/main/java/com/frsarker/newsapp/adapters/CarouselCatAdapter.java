package com.frsarker.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.frsarker.newsapp.PostActivity;
import com.frsarker.newsapp.R;
import java.util.ArrayList;
import java.util.HashMap;

public class CarouselCatAdapter extends RecyclerView.Adapter<CarouselCatAdapter.CarouselCatViewHolder> {
    Context context;
    ArrayList<HashMap<String, String>> dataList;

    public CarouselCatAdapter(Context context, ArrayList<HashMap<String, String>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public static class CarouselCatViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView rowCategoryName;
        public TextView rowCategoryNameSingle;
        public CardView itemContainer;

        public CarouselCatViewHolder(View view) {
            super(view);
            this.rowCategoryName = view.findViewById(R.id.row_category_name);
            this.rowCategoryNameSingle = view.findViewById(R.id.row_category_name_single);
            this.itemContainer = view.findViewById(R.id.itemContainer);
        }
    }

    public CarouselCatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_carousel_cat, parent, false);
        CarouselCatViewHolder viewHolder = new CarouselCatViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselCatViewHolder holder, int position) {
        final HashMap<String, String> singleTask = dataList.get(position);

        holder.rowCategoryName.setText(singleTask.get("cat_name"));
        holder.rowCategoryNameSingle.setText(singleTask.get("cat_name").toUpperCase().charAt(0)+"");

        int color_pos = Integer.parseInt(singleTask.get("color_pos"));
        int[] rainbow = context.getResources().getIntArray(R.array.rainbow);
        holder.itemContainer.setCardBackgroundColor(rainbow[color_pos]);

        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostActivity.class);
                i.putExtra("cat_id", singleTask.get("cat_id"));
                i.putExtra("cat_name", singleTask.get("cat_name"));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
