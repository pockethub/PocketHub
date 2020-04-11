package com.github.pockethub.android.markwon;

import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.noties.markwon.AbstractMarkwonPlugin;

public class AsyncDrawableSchedulerPlugin extends AbstractMarkwonPlugin {
    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        textView.removeOnLayoutChangeListener(this::onLayoutChange);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        textView.addOnLayoutChangeListener(this::onLayoutChange);
    }

    private void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v instanceof TextView) {
            io.noties.markwon.image.AsyncDrawableScheduler.schedule((TextView) v);
        }
    }
}