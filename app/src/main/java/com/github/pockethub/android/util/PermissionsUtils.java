package com.github.pockethub.android.util;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by savio on 2017-01-08.
 */
public class PermissionsUtils {

    public static void askForPermission(final Activity activity, final int requestCode,
                                        final String permission, @StringRes final int askTitle,
                                        @StringRes final int askContent) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                    .title(askTitle)
                    .content(askContent)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive((dialog, which) ->
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{permission}, requestCode));

            builder.show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    public static void askForPermission(final Fragment fragment, final int requestCode,
                                        final String permission, @StringRes final int askTitle,
                                        @StringRes final int askContent) {

        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(fragment.getActivity())
                    .title(askTitle)
                    .content(askContent)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive((dialog, which) ->
                            fragment.requestPermissions(new String[]{permission}, requestCode));

            builder.show();
        } else {
            fragment.requestPermissions(new String[]{permission}, requestCode);
        }
    }
}
