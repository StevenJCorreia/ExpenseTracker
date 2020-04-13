package com.stevenjcorreia.expensetracker;

import android.net.Uri;
import android.os.Environment;

class Utils {
    static final int IMPORT = 1;
    static final int EXPORT = 2;
    static final int CREATE_FILE = 3;
    static final int PICK_FILE = 4;

    static Uri getInitialDirectory() {
        return hasExternalStorage() ? Uri.parse(Environment.getExternalStorageState()) : Uri.fromFile(Environment.getDataDirectory());
    }

    public static int getMonth(String month) {
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
}
