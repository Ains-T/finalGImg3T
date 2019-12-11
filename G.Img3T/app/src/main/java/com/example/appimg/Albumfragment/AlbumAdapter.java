package com.example.appimg.Albumfragment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String >> data;

    public AlbumAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
        this.activity = activity;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumViewHolder holder = null;
        if(convertView == null){
            holder =new AlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.album_item, parent, false);
            holder.galleryImage = (ImageView) convertView.findViewById(R.id.album_item_iv_galleryImage);
            holder.gallery_count = (TextView) convertView.findViewById(R.id.album_item_tv_gallery_count);
            holder.gallery_title = (TextView) convertView.findViewById(R.id.album_item_tv_gallery_title);

            convertView.setTag(holder);
        }
        else {
            holder = (AlbumViewHolder) convertView.getTag();
        }
        holder.galleryImage.setId(position);
        holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        HashMap<String, String> s = new HashMap<String, String>();
        s = data.get(position);
        try {
            holder.gallery_title.setText(s.get(Function.KEY_ALBUM));
            holder.gallery_count.setText(s.get(Function.KEY_COUNT));

            Glide.with(activity)
                    .load(new File(s.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);


        } catch (Exception e) {
        }
        return convertView;
    }

    static class AlbumViewHolder {
        ImageView galleryImage;
        TextView gallery_count, gallery_title;
    }
}
