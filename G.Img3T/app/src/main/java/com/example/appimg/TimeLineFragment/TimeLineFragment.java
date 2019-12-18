package com.example.appimg.TimeLineFragment;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.appimg.FullImager.Full_Image;
import com.example.appimg.ImagesFragment.ImageFragment;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.example.appimg.Utils.MapComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class TimeLineFragment extends Fragment {
    //ViewFlipper viewFlipper;
    static final int REQUEST_PERMISSION_KEY = 1;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> listfiles = new ArrayList<HashMap<String, String>>();
    GridView gridView;
    TextView name, time;
    ViewFlipper viewFlipper;
    LoadImage loadImagetask;
    Animation in, out;
    boolean pickintent = false;
    private TimeLineAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.fragment_timeline_vf_time);
        gridView = (GridView) view.findViewById(R.id.fragment_timeline_gv_item);
        name = (TextView) view.findViewById(R.id.fragment_timeline_tv_name);
        time = (TextView) view.findViewById(R.id.fragment_timeline_tv_date);

        in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);
        viewFlipper.setFlipInterval(4000);
        viewFlipper.setAutoStart(true);
        return view;
    }

    class LoadImage extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listfiles.clear();
            imageList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            listfiles.clear();
            imageList.clear();
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExternal = getContext().getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursorInternal = getContext().getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Function.getCount(getContext().getApplicationContext(), album);
                listfiles.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
            }
            cursor.close();
            for (int i = 0; i < listfiles.size(); i++){
                Cursor cursorExternal1 = getContext().getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + listfiles.get(i).get(Function.KEY_ALBUM) + "\"", null, null);
                Cursor cursorInternal1 = getContext().getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + listfiles.get(i).get(Function.KEY_ALBUM) + "\"", null, null);
                Cursor cursor1 = new MergeCursor(new Cursor[]{cursorExternal1, cursorInternal1});
                while (cursor1.moveToNext()) {

                    path = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                    imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
                }
                cursor1.close();

            }
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            adapter = new TimeLineAdapter(getActivity(), imageList);
            gridView.setAdapter(adapter);
            //Toast.makeText(getContext(),String.valueOf(imageList.size()), Toast.LENGTH_SHORT).show();
            //tạo slide show
            int a = 0;
            for(int i=0; i<imageList.size(); i++){
                Random rand = new Random();
                final int n = rand.nextInt(imageList.size());
                ImageView img = new ImageView(getContext());
                Glide.with(getActivity()).load(new File(imageList.get(n).get(Function.KEY_PATH))).into(img);
//                String ten = imageList.get(n).get(Function.KEY_PATH);
//                ten = ten.substring(ten.lastIndexOf("/")+1);
//                name.setText(ten);
                a = n;
                viewFlipper.addView(img);

            }

            //Click flipper
            final int finalA = a;
            viewFlipper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!pickintent){
                        Intent intent = new Intent(getContext(), Full_Image.class);


                        String imgPath = imageList.get(finalA).get(Function.KEY_PATH);
                        intent.putExtra("title", imgPath);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list",imageList);
                        intent.putExtras(bundle);


                        intent.putExtra("position", finalA);
                        startActivity(intent);
                    }else{
                        String imgPath = imageList.get(finalA).get(Function.KEY_PATH);
                        Uri imguri = Uri.fromFile(new File(imgPath));
                        Intent intentResult = new Intent();
                        intentResult.setData(imguri);
                        getActivity().setResult(Activity.RESULT_OK,intentResult);
                        getActivity().finish();
                    }
                }
            });

            //click image
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!pickintent){
                        Intent intent = new Intent(getContext(), Full_Image.class);


                        String imgPath = imageList.get(+position).get(Function.KEY_PATH);
                        intent.putExtra("title", imgPath);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list",imageList);
                        intent.putExtras(bundle);


                        intent.putExtra("position", position);
                        startActivity(intent);
                    }else{
                        String imgPath = imageList.get(+position).get(Function.KEY_PATH);
                        Uri imguri = Uri.fromFile(new File(imgPath));
                        Intent intentResult = new Intent();
                        intentResult.setData(imguri);
                        getActivity().setResult(Activity.RESULT_OK,intentResult);
                        getActivity().finish();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(getContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        } else {
            loadImagetask = new LoadImage();
            loadImagetask.execute();
        }
    }
}
