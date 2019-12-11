package com.example.appimg.ImageAlbum;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appimg.FullImager.Full_Image;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.example.appimg.Utils.MapComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ImageAlbumView extends AppCompatActivity {

    String album_name = "";
    boolean pickintent = false;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    LoadAlbumImages loadAlbumImagesTasks;
    ImageAlbumAdapter adapter;
    GridView gridView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_album);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        Intent intent = getIntent();
        album_name = intent.getStringExtra("name");

        pickintent = intent.getExtras().getBoolean("pickintent", false);

        //set title và trở lại
        setTitle(album_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gridView = (GridView) findViewById(R.id.view_image_album_gv_item);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            gridView.setColumnWidth(Math.round(px));
        }


        loadAlbumImagesTasks = new LoadAlbumImages();
        loadAlbumImagesTasks.execute();
    }
    //tạo menu sắp xếp ảnh
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //set sự kiện trở về và sự kiện sắp xếp
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.sort_sort:
                reorder();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //sắp xếp ảnh
    private void reorder() {
        //  Collections.sort(albumList, new MapComparator(Function.KEY_ALBUM, "dsc"));
        //  adapter.notifyDataSetChanged();
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.reoder_dialog);
        dialog.setTitle("Sort...");
        final RadioGroup radioGroup = dialog.findViewById(R.id.reoder_dialog_radio);
        final RadioGroup radioGroup2 = dialog.findViewById(R.id.reoder_dialog_radio2);
        Button dialogButton = (Button) dialog.findViewById(R.id.reoder_dialog_bt_sort);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String order = "dsc";
                switch (radioGroup2.getCheckedRadioButtonId()) {
                    case R.id.reoder_dialog_asc:
                        order = "asc";
                        break;
                    case R.id.reoder_dialog_dsc:
                        order = "dsc";
                        break;
                    default:

                }
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.reoder_dialog_time:
                        Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, order));
                        break;
                    case R.id.reoder_dialog_name:
                        String name = Function.KEY_PATH;
                        name = name.substring(name.lastIndexOf("/") + 1);
                        Collections.sort(imageList, new MapComparator(name, order));
                        break;
                    case R.id.reoder_dialog_path:
                        Collections.sort(imageList, new MapComparator(Function.KEY_PATH, order));
                        break;
                    default:
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    //--------------------------------------


    //hiển thị ảnh trong album ra màn hình
    class LoadAlbumImages extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            imageList.clear();
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};

            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            adapter = new ImageAlbumAdapter(ImageAlbumView.this, imageList);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!pickintent){
                        Intent intent = new Intent(ImageAlbumView.this, Full_Image.class);
                        Bundle bundle = new Bundle();
                        String imgPath = imageList.get(+position).get(Function.KEY_PATH);
                        intent.putExtra("title", imgPath);
                        bundle.putSerializable("list",imageList);
                        intent.putExtras(bundle);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }else{
                        String imgPath = imageList.get(+position).get(Function.KEY_PATH);
                        Uri imguri = Uri.fromFile(new File(imgPath));
                        Intent intentResult = new Intent();
                        intentResult.setData(imguri);
                        setResult(Activity.RESULT_OK,intentResult);
                        finish();
                    }
                }
            });
        }
    }
}
