package com.example.appimg.TimeLineFragment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class TimeLineAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public TimeLineAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
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
        ImgHolder holder = null;
        if(convertView == null){
            holder = new ImgHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.timeline_item, parent, false);

            holder.images = (ImageView) convertView.findViewById(R.id.timeline_item_iv_image);
            convertView.setTag(holder);
        }
        else {
            holder = (ImgHolder) convertView.getTag();
        }
        holder.images.setId(position);
        HashMap<String, String> hm = new HashMap<String, String>();
        hm = data.get(position);

        try {
            Glide.with(activity).load(new File(hm.get(Function.KEY_PATH)))
                    .into(holder.images);
        }catch (Exception ignored){}

        return convertView;
    }

    static class ImgHolder{
        ImageView images;
    }
}
