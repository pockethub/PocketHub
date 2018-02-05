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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.MarkdownLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.Repository;
import javax.inject.Inject;

import butterknife.BindView;

/**
 * Fragment to display rendered comment fragment
 */
public class RenderedCommentFragment extends DialogFragment {

    private static final String ARG_TEXT = "text";

    private static final String ARG_REPO = "repo";

    @BindView(R.id.pb_loading)
    protected ProgressBar progress;

    @BindView(R.id.tv_comment_body)
    protected TextView bodyText;

    @Inject
    protected HttpImageGetter imageGetter;

    /**
     * Set text to render
     *
     * @param raw
     * @param repo
     */
    public void setText(final String raw, final Repository repo) {
        loadMarkdown(raw, repo);
        hideSoftKeyboard();
        showLoading(true);
    }

    private void hideSoftKeyboard() {
        Context context = getContext();
        if (context != null) {
            InputMethodManager imm =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(bodyText.getWindowToken(), 0);
            }
        }
    }

    private void showLoading(final boolean loading) {
        if (loading) {
            progress.setVisibility(View.VISIBLE);
            bodyText.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
            bodyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_preview, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void loadMarkdown(String raw, Repository repo) {
        MarkdownLoader.load(getActivity(), raw, repo, imageGetter, true)
                .subscribe(rendered -> {
                    bodyText.setText(rendered);
                    showLoading(false);
                } , e -> ToastUtils.show(getActivity(), R.string.error_rendering_markdown));
    }
}
