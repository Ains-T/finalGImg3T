package com.example.appimg.ImageAlbum;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.appimg.ImagesFragment.ImageAdapter;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageAlbumAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public ImageAlbumAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
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
        ImgAlbumHolder holder = null;
        if(convertView == null){
            holder = new ImgAlbumHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.item_view_image_album, parent, false);

            holder.images = (ImageView) convertView.findViewById(R.id.item_view_image_album_iv_image);
            convertView.setTag(holder);
        }
        else {
            holder = (ImgAlbumHolder) convertView.getTag();
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

    static class ImgAlbumHolder{
        ImageView images;
    }
}
