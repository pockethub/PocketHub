/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui;

import static com.github.mobile.R.string.blank_field_warning;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import roboguice.inject.InjectResource;

/**
 * Helper to set error text when {@link EditText} is blank
 */
public class BlankTextFieldWarner implements OnFocusChangeListener {

    @InjectResource(blank_field_warning)
    private String warning;

    @Override
    public void onFocusChange(final View view, final boolean hasFocus) {
        EditText editText = (EditText) view;
        if (editText.length() == 0 && !hasFocus)
            editText.setError(warning);
    }
}
