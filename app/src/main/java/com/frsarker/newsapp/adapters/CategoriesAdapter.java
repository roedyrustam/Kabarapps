package com.frsarker.newsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.frsarker.newsapp.R;
import java.util.ArrayList;
import java.util.HashMap;

public class CategoriesAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> dataList;

    public CategoriesAdapter(Context c, ArrayList<HashMap<String, String>> d) {
        context = c;
        dataList = d;
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CategoriesViewHolder holder = null;
        if (convertView == null) {
            holder = new CategoriesViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_categories, parent, false);
            holder.rowCategoryName = convertView.findViewById(R.id.row_category_name);
            holder.rowCategoryNameSingle = convertView.findViewById(R.id.row_category_name_single);
            holder.itemContainer = convertView.findViewById(R.id.itemContainer);
            convertView.setTag(holder);
        } else {
            holder = (CategoriesViewHolder) convertView.getTag();
        }

        final HashMap<String, String> singleTask = dataList.get(position);
        holder.rowCategoryName.setId(position);
        holder.rowCategoryNameSingle.setId(position);
        holder.itemContainer.setId(position);

        try {
            holder.rowCategoryName.setText(singleTask.get("cat_name"));
            holder.rowCategoryNameSingle.setText(singleTask.get("cat_name").toUpperCase().charAt(0)+"");

            int color_pos = Integer.parseInt(singleTask.get("color_pos"));
            int[] rainbow = context.getResources().getIntArray(R.array.rainbow);
            holder.itemContainer.setCardBackgroundColor(rainbow[color_pos]);
        } catch (Exception e) {
        }
        return convertView;
    }
}

class CategoriesViewHolder {
    TextView rowCategoryName, rowCategoryNameSingle;
    CardView itemContainer;
}