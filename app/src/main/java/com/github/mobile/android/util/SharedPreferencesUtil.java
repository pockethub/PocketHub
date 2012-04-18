package com.github.mobile.android.util;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {

    private static boolean isEditorApplyAvailable() {
        return SDK_INT >= GINGERBREAD;
    }

    /**
     * Uses the non-blocking apply() method on post-Froyo devices, otherwise
     * use the older commit() method which blocks while doing IO.
     *
     * @param editor
     */
    public static void savePrefsFrom(Editor editor) {
        if (isEditorApplyAvailable()) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
