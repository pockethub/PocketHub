package com.github.mobile.android.ui.validation;

import android.view.View;
import android.widget.EditText;
import com.github.mobile.android.R;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;

import static com.github.mobile.android.R.string.blank_field_warning;


public class LeavingBlankTextFieldWarner implements View.OnFocusChangeListener {

    @InjectResource(blank_field_warning) String warning;

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        EditText editText = (EditText) view;
        if (editText.length() == 0 && !hasFocus) {
            editText.setError(warning);
        }
    }
}
