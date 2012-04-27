package com.github.mobile.util;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Utility class for working with {@link SharedPreferences}
 */
public class PreferenceUtils {

    private static boolean isEditorApplyAvailable() {
        return SDK_INT >= GINGERBREAD;
    }

    /**
     * Save preferences in given editor
     *
     * @param editor
     */
    public static void save(Editor editor) {
        if (isEditorApplyAvailable())
            editor.apply();
        else
            editor.commit();
    }
}
