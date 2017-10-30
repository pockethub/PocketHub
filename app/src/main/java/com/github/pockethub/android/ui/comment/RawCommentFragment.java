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
package com.github.pockethub.android.ui.comment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.TextWatcherAdapter;

import java.io.IOException;

import com.github.pockethub.android.util.ImageBinPoster;
import com.github.pockethub.android.util.PermissionsUtils;
import com.github.pockethub.android.util.ToastUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Fragment to display raw comment text
 */
public class RawCommentFragment extends DialogFragment {

    private static final int REQUEST_CODE_SELECT_PHOTO = 0;
    private static final int READ_PERMISSION_REQUEST = 1;

    private EditText commentText;

    private FloatingActionButton addImageFab;

    /**
     * Text to populate comment window.
     */
    private String initComment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentText = (EditText) view.findViewById(R.id.et_comment);
        addImageFab = (FloatingActionButton) view.findViewById(R.id.fab_add_image);

        // @TargetApi(…) required to ensure build passes
        // noinspection Convert2Lambda
        addImageFab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Fragment fragment = RawCommentFragment.this;
                    String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

                    if (ContextCompat.checkSelfPermission(getActivity(), permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        PermissionsUtils.askForPermission(fragment, READ_PERMISSION_REQUEST,
                                permission, R.string.read_permission_title,
                                R.string.read_permission_content);
                    } else {
                        startImagePicker();
                    }
                } else {
                    startImagePicker();
                }
            }
        });

        commentText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }
        });
        commentText.setOnTouchListener((v, event) -> {
            commentText.requestFocusFromTouch();
            return false;
        });

        setText(initComment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PERMISSION_REQUEST) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }

            if (result) {
                startImagePicker();
            }
        }
    }

    private void startImagePicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            showProgressIndeterminate(R.string.loading);
            ImageBinPoster.post(getActivity(), data.getData(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dismissProgress();
                    showImageError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        insertImage(ImageBinPoster.getUrl(response.body().string()));
                    } else {
                        showImageError();
                    }
                }
            });
        }
    }

    private void showImageError() {
        ToastUtils.show(getActivity(), R.string.error_image_upload);
    }

    private void insertImage(final String url) {
        getActivity().runOnUiThread(() -> commentText.append("![](" + url + ")"));
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
