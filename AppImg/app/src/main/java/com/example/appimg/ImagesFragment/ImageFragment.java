package com.example.appimg.ImagesFragment;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.example.appimg.Utils.MapComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ImageFragment extends Fragment {

    static final int REQUEST_PERMISSION_KEY = 1;
    LoadAll loadAlltasks;
    ArrayList<HashMap<String, String>> listfiles = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    GridView gridView;
    String imageFilePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_img, container, false);

        gridView = (GridView) view.findViewById(R.id.fragment_img_gv_item);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getContext().getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if(dp < 360){
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getContext().getApplicationContext());
            gridView.setColumnWidth(Math.round(px));
        }
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(getContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        }
        return view;
    }

    class LoadAll extends AsyncTask<String, Void, String>{
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
//            Collections.sort(imageList, (Comparator<? super HashMap<String, String>>) new MapComparator(Function.KEY_PATH, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            ImageAdapter adapter = new ImageAdapter(getActivity(), imageList);
            gridView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(getContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        } else {
            loadAlltasks = new LoadAll();
            loadAlltasks.execute();
        }
    }
}
