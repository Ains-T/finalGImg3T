package com.example.appimg.ImagesFragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.appimg.FullImager.Full_Image;
import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.example.appimg.Utils.MapComparator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ImageFragment extends Fragment {

    static final int REQUEST_PERMISSION_KEY = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    LoadAll loadAlltasks;
    ArrayList<HashMap<String, String>> listfiles = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    ImageAdapter adapter;
    GridView gridView;
    boolean pickintent = false;
    String imageFilePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_img, container, false);
        setHasOptionsMenu(true);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mains_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mains_action_sort:
                // search action
                reorder();
                return true;
            case R.id.mains_action_camera:
                cameraFunction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void reorder() {
        //  Collections.sort(albumList, new MapComparator(Function.KEY_ALBUM, "dsc"));
        //  adapter.notifyDataSetChanged();
        // custom dialog
        final Dialog dialog = new Dialog(getContext());
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
                        Collections.sort(imageList, new MapComparator(Function.KEY_ALBUM, order));
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

    private void cameraFunction(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        openCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        makeToast();
                    }

                    void makeToast() {
                        Toast.makeText(getActivity(), "You must accept camera permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
    }

    private void openCamera() {
        if (isDeviceSupportCamera()) {
            openCameraIntent();
        } else
            Toast.makeText(getActivity(), "Device doesn't support camera", Toast.LENGTH_SHORT).show();
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.appimg.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                try {
                    startActivityForResult(pictureIntent,
                            REQUEST_CAPTURE_IMAGE);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                //            getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private boolean isDeviceSupportCamera() {
        if (getContext().getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
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
            Collections.sort(imageList, new MapComparator(Function.KEY_PATH, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            adapter = new ImageAdapter(getActivity(), imageList);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!pickintent){
                        Intent intent = new Intent(getContext(), Full_Image.class);
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
            loadAlltasks = new LoadAll();
            loadAlltasks.execute();
        }
    }
}
