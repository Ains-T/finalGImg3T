package com.example.appimg.FullImager;

import android.media.ExifInterface;

import java.io.File;

public class ImageDetailsHelper {
    String title, time, width, height, filesize, path;
    ExifInterface exif;

    public ImageDetailsHelper(String path, ExifInterface exif) {
        this.path = path;
        this.exif = exif;
    }

    public String getTitle() {
        title = path.substring(path.lastIndexOf("/") + 1);
        return title;
    }

    public String getTime() {
        time = exif.getAttribute(ExifInterface.TAG_DATETIME);
        return time;
    }

    public String getWidth() {
        width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        return width;
    }

    public String getHeight() {
        height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        return height;
    }

    public String getFilesize() {
        File file = new File(path);
        filesize = String.valueOf((int) (file.length() / 1024));
        return filesize;
    }

    public String getPath() {
        return path;
    }
}
