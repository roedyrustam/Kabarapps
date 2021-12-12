package com.frsarker.newsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.PopupMenu;
import com.frsarker.newsapp.Config;
import com.frsarker.newsapp.DBHelper;
import com.frsarker.newsapp.Extras;
import com.frsarker.newsapp.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class ListAdapter extends BaseAdapter {
    private Context context;
    DBHelper mydb;
    private ArrayList<HashMap<String, String>> dataList;

    public ListAdapter(Context c, DBHelper db, ArrayList<HashMap<String, String>> d) {
        context = c;
        dataList = d;
        mydb = db;
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
        ListViewHolder holder = null;
        if (convertView == null) {
            holder = new ListViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_list, parent, false);
            holder.menu = convertView.findViewById(R.id.menu);
            holder.rowTitle = convertView.findViewById(R.id.row_title);
            holder.rowDate = convertView.findViewById(R.id.row_date);
            holder.rowCategoryName = convertView.findViewById(R.id.row_category_name);
            holder.rowThumbnail = convertView.findViewById(R.id.row_thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ListViewHolder) convertView.getTag();
        }

        final HashMap<String, String> singleTask = dataList.get(position);
        holder.menu.setId(position);
        holder.rowTitle.setId(position);
        holder.rowDate.setId(position);
        holder.rowCategoryName.setId(position);
        holder.rowThumbnail.setId(position);

        try {
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

            Picasso.get()
                    .load(Config.host+"/styles/files/thumbs/"+singleTask.get("post_cover"))
                    .resize(300, 200)
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .into(holder.rowThumbnail);
        } catch (Exception e) {
        }
        return convertView;
    }
}

class ListViewHolder {
    TextView rowTitle, rowDate, rowCategoryName;
    ImageButton menu;
    ImageView rowThumbnail;
}