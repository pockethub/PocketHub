package com.github.pockethub.android.rx;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import io.reactivex.SingleTransformer;

public final class RxProgress {

    private RxProgress() {
        throw new AssertionError("No instances.");
    }

    public static <U> SingleTransformer<U, U> bindToLifecycle(Context context, @StringRes int stringRes) {
        return bindToLifecycle(context, context.getString(stringRes));
    }

    public static <U> SingleTransformer<U, U> bindToLifecycle(Context context, CharSequence message) {
        return upstream -> {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                    .content(message)
                    .progress(true, 0)
                    .build();

            return upstream
                    .doOnSubscribe(disposable -> progressDialog.show())
                    .doOnSuccess(u -> progressDialog.dismiss())
                    .doOnError(throwable -> progressDialog.dismiss());
        };
    }
}
