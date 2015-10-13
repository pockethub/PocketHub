/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui.comment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.pockethub.R;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.TextWatcherAdapter;

/**
 * Fragment to display raw comment text
 */
public class RawCommentFragment extends DialogFragment {

    private EditText commentText;

    /**
     * Text to populate comment window.
     */
    private String initComment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentText = finder.find(R.id.et_comment);
        commentText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Activity activity = getActivity();
                if (activity != null)
                    activity.invalidateOptionsMenu();
            }
        });
        commentText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                commentText.requestFocusFromTouch();
                return false;
            }
        });

        setText(initComment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_create, container, false);
    }

    /**
     * Get comment text
     *
     * @return text
     */
    public String getText() {
        return commentText.getText().toString();
    }

    /**
     * Set comment text
     *
     * @return text
     */
    public void setText(String comment) {
        if (commentText != null) {
            commentText.setText(comment);
            commentText.selectAll();
        } else {
            initComment = comment;
        }
    }
}
