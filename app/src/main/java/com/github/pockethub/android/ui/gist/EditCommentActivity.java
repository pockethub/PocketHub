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
package com.github.pockethub.android.ui.gist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.service.gists.GistCommentService;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_GIST;

/**
 * Activity to edit a comment on a {@link Gist}
 */
public class EditCommentActivity extends
        com.github.pockethub.android.ui.comment.CreateCommentActivity {

    private static final String TAG = "EditCommentActivity";

    /**
     * Create intent to edit a comment
     *
     * @param gist
     * @return intent
     */
    public static Intent createIntent(Gist gist, GitHubComment comment) {
        Builder builder = new Builder("gist.comment.edit.VIEW");
        builder.gist(gist);
        builder.add(EXTRA_COMMENT, comment);
        return builder.toIntent();
    }

    private Gist gist;

    /**
     * Comment to edit.
     */
    private GitHubComment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        gist = getParcelableExtra(EXTRA_GIST);
        comment = getIntent().getParcelableExtra(EXTRA_COMMENT);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.gist_title) + gist.id());
        User user = gist.owner();
        if (user != null)
            actionBar.setSubtitle(user.login());
        avatars.bind(actionBar, user);
    }

    @Override
    protected void createComment(String comment) {
        CommentRequest commentRequest = CommentRequest.builder()
                .body(comment)
                .build();
        editComment(commentRequest);
    }

    /**
     * Edit comment.
     *
     * @param commentRequest
     */
    protected void editComment(CommentRequest commentRequest) {
        ServiceGenerator.createService(this, GistCommentService.class)
                .editGistComment(gist.id(), comment.id(), commentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GitHubComment>bindToLifecycle())
                .subscribe(new ProgressObserverAdapter<GitHubComment>(this, R.string.editing_comment) {

                    @Override
                    public void onNext(GitHubComment edited) {
                        super.onNext(edited);
                        dismissProgress();
                        finish(edited);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.d(TAG, "Exception editing comment on gist", e);
                        ToastUtils.show(EditCommentActivity.this, e.getMessage());
                    }
                }.start());
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        CommentPreviewPagerAdapter commentPreviewPagerAdapter = new CommentPreviewPagerAdapter(this, null);
        commentPreviewPagerAdapter.setCommentText(comment != null ? comment.body() : null);
        return commentPreviewPagerAdapter;
    }
}
