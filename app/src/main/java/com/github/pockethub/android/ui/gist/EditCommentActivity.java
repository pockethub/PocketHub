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

import com.alorma.github.sdk.bean.dto.request.CommentRequest;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.gists.EditGistCommentClient;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.HtmlUtils;
import com.github.pockethub.android.util.ToastUtils;

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
    public static Intent createIntent(Gist gist, GithubComment comment) {
        Builder builder = new Builder("gist.comment.edit.VIEW");
        builder.gist(gist);
        builder.add(EXTRA_COMMENT, comment);
        return builder.toIntent();
    }

    private Gist gist;

    /**
     * Comment to edit.
     */
    private GithubComment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        gist = getParcelableExtra(EXTRA_GIST);
        comment = getIntent().getParcelableExtra(EXTRA_COMMENT);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.gist_title) + gist.id);
        User user = gist.owner;
        if (user != null)
            actionBar.setSubtitle(user.login);
        avatars.bind(actionBar, user);
    }

    @Override
    protected void createComment(String comment) {
        editComment(comment);
    }

    /**
     * Edit comment.
     *
     * @param commentText
     */
    protected void editComment(String commentText) {
        new EditGistCommentClient(gist.id, comment.id, new CommentRequest(commentText)).observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GithubComment>bindToLifecycle())
                .subscribe(new ProgressObserverAdapter<GithubComment>(this, R.string.editing_comment) {

                    @Override
                    public void onNext(GithubComment edited) {
                        super.onNext(edited);
                        dismissProgress();
                        edited.body_html = HtmlUtils.format(edited.body_html).toString();
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
        commentPreviewPagerAdapter.setCommentText(comment != null ? comment.body : null);
        return commentPreviewPagerAdapter;
    }
}
