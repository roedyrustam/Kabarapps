package com.frsarker.newsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frsarker.newsapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> dataList;

    public CommentAdapter(Context c, ArrayList<HashMap<String, String>> d) {
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
        CommentViewHolder holder = null;
        if (convertView == null) {
            holder = new CommentViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_comment, parent, false);
            holder.rowUserName = convertView.findViewById(R.id.row_user_name);
            holder.rowThumbnail = convertView.findViewById(R.id.row_thumbnail);
            holder.rowDate = convertView.findViewById(R.id.row_date);
            holder.rowComment = convertView.findViewById(R.id.row_comment);
            convertView.setTag(holder);
        } else {
            holder = (CommentViewHolder) convertView.getTag();
        }

        final HashMap<String, String> singleTask = dataList.get(position);
        holder.rowUserName.setId(position);
        holder.rowThumbnail.setId(position);
        holder.rowDate.setId(position);
        holder.rowComment.setId(position);

        try {
            holder.rowUserName.setText(singleTask.get("user_name"));
            holder.rowDate.setText(singleTask.get("commented_at"));
            holder.rowComment.setText(singleTask.get("comment"));

            Picasso.get()
                    .load(singleTask.get("user_photo"))
                    .resize(300, 200)
                    .centerCrop()
                    .error(R.drawable.user_image)
                    .into(holder.rowThumbnail);


        } catch (Exception e) {
        }
        return convertView;
    }
}

class CommentViewHolder {
    TextView rowUserName, rowDate, rowComment;
    ImageView rowThumbnail;
}