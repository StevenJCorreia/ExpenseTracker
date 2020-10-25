package com.stevenjcorreia.expensetracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

class Utils {
    static final int CREATE_EXPORT_FILE = 3;
    static final int PICK_IMPORT_FILE = 4;
    static final int TAKE_IMAGE = 5;
    static final int PICK_IMAGE = 6;

    static Bitmap byteArrayToBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    static Uri getInitialDirectory() {
        return hasExternalStorage() ? Uri.parse(Environment.getExternalStorageState()) : Uri.fromFile(Environment.getDataDirectory());
    }

    static int getMonth(String month) {
        switch (month) {
            case "Jan":
                return 0;
            case "Feb":
                return 1;
            case "Mar":
                return 2;
            case "Apr":
                return 3;
            case "May":
                return 4;
            case "Jun":
                return 5;
            case "Jul":
                return 6;
            case "Aug":
                return 7;
            case "Sep":
                return 8;
            case "Oct":
                return 9;
            case "Nov":
                return 10;
            case "Dec":
                return 11;
        }

        return -1;
    }

    private static boolean hasExternalStorage() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        return isSDSupportedDevice && isSDPresent;
    }

    static byte[] imageViewToByteArray(ImageView image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return baos.toByteArray();
    }
}
