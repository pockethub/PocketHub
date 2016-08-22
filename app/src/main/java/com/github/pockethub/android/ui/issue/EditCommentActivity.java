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
package com.github.pockethub.android.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.request.CommentRequest;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.issues.EditIssueCommentClient;
import com.github.pockethub.android.Intents;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.HtmlUtils;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.pockethub.android.Intents.EXTRA_USER;

/**
 * Activity to edit a comment on an {@link Issue}
 */
public class EditCommentActivity extends
        com.github.pockethub.android.ui.comment.CreateCommentActivity {

    private static final String TAG = "EditCommentActivity";

    /**
     * Create intent to edit a comment
     *
     * @param repoId
     * @param issueNumber
     * @param user
     * @return intent
     */
    public static Intent createIntent(Repo repoId, int issueNumber, GithubComment comment,
            User user) {
        Builder builder = new Builder("issue.comment.edit.VIEW");
        builder.repo(repoId);
        builder.add(EXTRA_COMMENT, comment);
        builder.add(EXTRA_ISSUE_NUMBER, issueNumber);
        builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    private Repo repositoryId;

    /**
     * Comment to edit.
     */
    private GithubComment comment;

    private int issueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        comment = getParcelableExtra(EXTRA_COMMENT);
        issueNumber = getIntExtra(EXTRA_ISSUE_NUMBER);
        repositoryId = getParcelableExtra(Intents.EXTRA_REPOSITORY);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.issue_title) + issueNumber);
        actionBar.setSubtitle(InfoUtils.createRepoId(repositoryId));
        avatars.bind(actionBar, (User) getParcelableExtra(EXTRA_USER));
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
        RepoInfo info = InfoUtils.createRepoInfo(repositoryId);
        new EditIssueCommentClient(info, comment.id, new CommentRequest(commentText)).observable()
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
                        Log.d(TAG, "Exception editing comment on issue", e);
                        ToastUtils.show(EditCommentActivity.this, e.getMessage());
                    }
                }.start());
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        CommentPreviewPagerAdapter commentPreviewPagerAdapter = new CommentPreviewPagerAdapter(this, repositoryId);
        commentPreviewPagerAdapter.setCommentText(comment != null ? comment.body : null);
        return commentPreviewPagerAdapter;
    }
}
