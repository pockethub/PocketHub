package com.github.mobile.android.util;


import static android.os.Build.VERSION_CODES.GINGERBREAD;
import android.content.SharedPreferences;
import android.os.Build;

public class SharedPreferencesUtil {

    private static boolean isEditorApplyAvailable() {
        return Build.VERSION.SDK_INT >= GINGERBREAD;
    }

    /**
     * Uses the non-blocking apply() method on post-Froyo devices, otherwise
     * use the older commit() method which blocks while doing IO.
     */
    public static void savePrefsFrom(SharedPreferences.Editor editor) {
        if (isEditorApplyAvailable()) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
