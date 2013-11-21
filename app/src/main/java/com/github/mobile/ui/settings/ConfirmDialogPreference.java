package com.github.mobile.ui.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mobile.R;

/**
 * A Preference to use when confirming a user wants to perform a particular action
 */
public class ConfirmDialogPreference extends DialogPreference {

    public interface OnDialogClosed {
        public void onDialogClosed(String key, boolean result);
    }

    private OnDialogClosed dialogClosedListener;

    public ConfirmDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public View onCreateView(ViewGroup viewGroup) {
        View view = super.onCreateView(viewGroup);
        ((TextView)view.findViewById(android.R.id.title))
            .setTextColor(getContext().getResources().getColor(R.color.text));
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        persistBoolean(positiveResult);
        if (dialogClosedListener != null)
            dialogClosedListener.onDialogClosed(getKey(), positiveResult);
    }

    public void setOnDialogClosedListener(OnDialogClosed listener) {
        dialogClosedListener = listener;
    }
}