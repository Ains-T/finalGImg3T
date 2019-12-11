package com.example.appimg.FullImager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FullImageAdapter extends PagerAdapter {
    ArrayList<HashMap<String, String>> imgPaths;
    private Activity activity;
    private LayoutInflater inflater;

    public FullImageAdapter(ArrayList<HashMap<String, String>> imgPaths, Activity activity) {
        this.imgPaths = imgPaths;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return this.imgPaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((RelativeLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView imgDisplay;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_fullimage, container, false);
        imgDisplay = (PhotoView) view.findViewById(R.id.layout_fullimage_pv_view);
        Glide.with(activity).load(new File(imgPaths.get(+position).get(Function.KEY_PATH)))
                .into(imgDisplay);

        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Full_Image) activity).setToolbarView();
            }
        });

        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}
