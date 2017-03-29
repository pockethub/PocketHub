package com.github.pockethub.android.rx;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class ProgressObserverAdapter<T> implements Observer<T>, SingleObserver<T> {

    private MaterialDialog progressDialog;
    private Context context;
    private String message;

    public ProgressObserverAdapter(Context context) {
        this(context, null);
    }

    public ProgressObserverAdapter(Context context, @StringRes int resid) {
        this(context, context.getString(resid));
    }

    public ProgressObserverAdapter(Context context, String message) {
        this.context = context;
        this.message = message;
    }

    public void setContent(@StringRes int resid){
        message = context.getString(resid);
    }

    public void setContent(String message){
        this.message = message;
    }

    @Override
    public void onSuccess(T t) {
        onComplete();
    }

    @Override
    public void onComplete() {
        dismissProgress();
    }

    @Override
    public void onSubscribe(final Disposable d) {
    }

    @Override
    public void onNext(final T t) {
    }

    @Override
    public void onError(Throwable e) {
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
        showProgressIndeterminate(context.getString(resId));
    }

    public ProgressObserverAdapter<T> start() {
        showProgressIndeterminate(message);
        return this;
    }
}
