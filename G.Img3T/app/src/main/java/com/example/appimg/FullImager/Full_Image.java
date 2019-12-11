package com.example.appimg.FullImager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.appimg.R;
import com.example.appimg.Utils.Function;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Full_Image extends AppCompatActivity  implements ViewPager.OnPageChangeListener{

    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    private ViewPager viewPager;
    Toolbar toolbar;
    LinearLayout bottomBar;
    private FullImageAdapter adapter;
    boolean mToolbarVisibility = true;
    ImageButton ib_crop, ib_delete;
    String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        toolbar = findViewById(R.id.activity_full_img_tb_bar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.activity_full_img_vp_viewpager);
        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);
        String title = i.getStringExtra("title");


        //kết nối dữ liệu hiển thị ảnh sau khi chọn ảnh cân xem
        imageList = (ArrayList<HashMap<String, String>>) i.getExtras().getSerializable("list");
        adapter = new FullImageAdapter(imageList, Full_Image.this);
        viewPager.setAdapter(adapter);

        //set title và nút trở về trang chính
        title = title.substring(title.lastIndexOf("/") + 1);
        if (title.length() > 15) {
            title = title.substring(0, 15).concat("...");
        }
        setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(this);
        setBottomBar();

    }
    //set Title
    void setTitle() {
        String path = imageList.get(+viewPager.getCurrentItem()).get(Function.KEY_PATH);
        String title = "";
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            ImageDetailsHelper details = new ImageDetailsHelper(path, exifInterface);
            title = details.getTitle();
            if (title.length() > 15) {
                title = title.substring(0, 15).concat("...");
            }
            getSupportActionBar().setTitle(title);
            //Toast.makeText(Full_Image.this, details.getTime(), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
            //        Toast.makeText(GalleryPreview.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //click để ẩn toolbar và bottombar
    void setToolbarView() {
        if (mToolbarVisibility) {
            getSupportActionBar().hide();
            bottomBar.setVisibility(View.GONE);
            //bottomBar.animate().translationY(bottomBar.getHeight()).setInterpolator(new DecelerateInterpolator(1));
            //    mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        } else {
            getSupportActionBar().show();
            bottomBar.setVisibility(View.VISIBLE);
            //bottomBar.animate().translationY(0).setInterpolator(new AccelerateInterpolator(1));

            //     mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        }
        mToolbarVisibility = !mToolbarVisibility;

    }

    //set menu thêm buttom xem thông tin
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.info_info:
                setImageDetails();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //sự kiện xem thông tin ảnh
    private void setImageDetails() {
        String path = imageList.get(+viewPager.getCurrentItem()).get(Function.KEY_PATH);
        String title = "", time = "", width = "", height = "", filesize = "";
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            ImageDetailsHelper details = new ImageDetailsHelper(path, exifInterface);
            title = details.getTitle();
            time = details.getTime();
            width = details.getWidth();
            height = details.getHeight();
            filesize = details.getFilesize() + " KB";
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.image_detail_dialog);
        dialog.setTitle("Info Image");
        TextView tv_path, tv_title, tv_time, tv_width, tv_height, tv_filesize;
        tv_path = dialog.findViewById(R.id.image_detail_dialog_path);
        tv_title = dialog.findViewById(R.id.image_detail_dialog_title);
        tv_time = dialog.findViewById(R.id.image_detail_dialog_time);
        tv_width = dialog.findViewById(R.id.image_detail_dialog_width);
        tv_height = dialog.findViewById(R.id.image_detail_dialog_height);
        tv_filesize = dialog.findViewById(R.id.image_detail_dialog_filesize);
        tv_path.setText(path);
        tv_title.setText(title);
        tv_time.setText(time);
        tv_width.setText(width);
        tv_height.setText(height);
        tv_filesize.setText(filesize);

        Button dialogButton = (Button) dialog.findViewById(R.id.image_detail_dialog_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
    //--------------------------------

    //bottombar
    private void setBottomBar(){
        bottomBar = findViewById(R.id.activity_full_img_ll_bottomBar);
        ib_crop = findViewById(R.id.activity_full_img_ib_crop);
        ib_delete= findViewById(R.id.activity_full_img_ib_delete);
        ib_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropFuntion();
            }
        });
        ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog();
            }
        });
    }
    //--------------------------------

    //sự kiện cắt ảnh
    private void cropFuntion() {
        String path = imageList.get(+viewPager.getCurrentItem()).get(Function.KEY_PATH);
        Uri inputUri = Uri.fromFile(new File(path));
        File photoFile = null;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_";
            File storageDir =
                    //            getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            photoFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            imageFilePath = photoFile.getAbsolutePath();

        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.appimg.provider", photoFile);
            Crop.of(inputUri, photoURI).asSquare().start(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                galleryAddPic();
            } else if (resultCode == RESULT_CANCELED) {
                deleteFile();
            }
        }
    }

    private void deleteFile() {
        try {
            File file = new File(imageFilePath);
            boolean deleted = file.delete();
        } catch (Exception e) {
        }
    }

    private void galleryAddPic() {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(imageFilePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //kết thúc sự kiện cắt ảnh
    //------------------------------------

    //set sự kiện xoá ảnh
    private void deleteDialog(){
        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteFileFromPath();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(Full_Image.this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClick)
                .setNegativeButton("No", dialogClick).show();
    }

    void deleteFileFromPath() {
        String path = imageList.get(+viewPager.getCurrentItem()).get(Function.KEY_PATH);
        File file = new File(path);
        // thiết lập phép chiếu (ID)
        String[] projection = {MediaStore.Images.Media._ID};

        // thiết lập đường dẫn tệp phù hợp
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        // truy vấn ID của media phù hợp với đường dẫn tệp
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
            c.close();
            Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // File not found in media store DB
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
    }
    //kết thúc sự kiện xoá ảnh

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setTitle();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
