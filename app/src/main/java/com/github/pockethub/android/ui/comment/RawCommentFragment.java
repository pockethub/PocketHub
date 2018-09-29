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
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.TextWatcherAdapter;

import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.util.ImageBinPoster;
import com.github.pockethub.android.util.PermissionsUtils;
import com.github.pockethub.android.util.ToastUtils;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment to display raw comment text
 */
public class RawCommentFragment extends BaseFragment {

    private static final int REQUEST_CODE_SELECT_PHOTO = 0;
    private static final int READ_PERMISSION_REQUEST = 1;

    @BindView(R.id.et_comment)
    protected EditText commentText;

    @BindView(R.id.fab_add_image)
    protected FloatingActionButton addImageFab;

    /**
     * Text to populate comment window.
     */
    private String initComment;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addImageFab.setOnClickListener(v -> {
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
            ImageBinPoster.post(getActivity(), data.getData())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(getActivity(), R.string.loading))
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response -> {
                        if (response.isSuccessful()) {
                            insertImage(ImageBinPoster.getUrl(response.body().string()));
                        } else {
                            showImageError();
                        }
                    }, throwable -> showImageError());
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
