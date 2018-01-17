package com.github.pockethub.android.ui.item;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

public class TextItem extends Item<ViewHolder> {

    private final int layoutId;
    private final int textViewId;
    private final CharSequence text;

    public TextItem(@LayoutRes int layoutId, @IdRes int textViewId, CharSequence text) {
        super(text.hashCode());
        this.layoutId = layoutId;
        this.textViewId = textViewId;
        this.text = text;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        TextView textView = viewHolder.getRoot().findViewById(textViewId);
        textView.setText(text);
    }

    @Override
    public int getLayout() {
        return layoutId;
    }
}
