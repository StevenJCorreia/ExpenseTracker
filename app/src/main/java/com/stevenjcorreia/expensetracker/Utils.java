package com.stevenjcorreia.expensetracker;

import android.net.Uri;
import android.os.Environment;

class Utils {
    static final int IMPORT = 1;
    static final int EXPORT = 2;
    static final int CREATE_FILE = 3;
    static final int PICK_FILE = 4;

    public static Uri getInitialDirectory() {
        return hasExternalStorage() ? Uri.parse(Environment.getExternalStorageState()) : Uri.fromFile(Environment.getDataDirectory());
    }

    private static boolean hasExternalStorage() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        return isSDSupportedDevice && isSDPresent;
    }
}
