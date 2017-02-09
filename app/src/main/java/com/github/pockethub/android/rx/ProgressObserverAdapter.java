package com.github.pockethub.android.rx;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

public class ProgressObserverAdapter<T> extends ObserverAdapter<T> {

    private MaterialDialog progressDialog;
    private Context context;

    private int resid;
    private String message;

    public ProgressObserverAdapter(Context context, @StringRes int resid) {
        this(context);
        this.resid = resid;
    }

    public ProgressObserverAdapter(Context context, String message) {
        this(context);
        this.message = message;
    }

    public ProgressObserverAdapter(Context context) {
        this.context = context;
    }

    public void setContent(@StringRes int resid){
        this.resid = resid;
    }

    public void setContent(String message){
        this.message = message;
    }

    @Override
    public void onCompleted() {
        super.onCompleted();
        dismissProgress();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        dismissProgress();
    }

    /**
     * Dismiss and clear progress dialog field
     */
    protected void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param message
     */
    protected void showProgressIndeterminate(final CharSequence message) {
        dismissProgress();
        progressDialog = new MaterialDialog.Builder(context)
                .content(message)
                .progress(true, 0)
                .build();
        progressDialog.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param resId
     */
    protected void showProgressIndeterminate(@StringRes final int resId) {
        dismissProgress();
        progressDialog = new MaterialDialog.Builder(context)
                .content(resId)
                .progress(true, 0)
                .build();
        progressDialog.show();
    }

    public ProgressObserverAdapter<T> start() {
        if(message == null)
            showProgressIndeterminate(resid);
        else
            showProgressIndeterminate(message);
        return this;
    }
}
