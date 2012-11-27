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
package com.github.mobile.ui.comment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

/**
 * Fragment to display raw comment text
 */
public class RawCommentFragment extends RoboSherlockFragment {

    private EditText commentText;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewFinder finder = new ViewFinder(view);
        commentText = finder.find(id.et_comment);
        commentText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                Activity activity = getActivity();
                if (activity != null)
                    activity.invalidateOptionsMenu();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.comment_create, null);
    }

    /**
     * Get comment text
     *
     * @return text
     */
    public String getText() {
        return commentText.getText().toString();
    }
}
