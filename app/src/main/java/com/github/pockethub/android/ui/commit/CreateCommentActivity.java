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
package com.github.pockethub.android.ui.commit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;

import com.alorma.github.sdk.bean.dto.request.CommitCommentRequest;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.commit.PublishCommitCommentClient;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.HtmlUtils;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_BASE;
import static com.github.pockethub.android.Intents.EXTRA_PATH;
import static com.github.pockethub.android.Intents.EXTRA_POSITION;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Activity to create a comment on a commit
 */
public class CreateCommentActivity extends
        com.github.pockethub.android.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param repository
     * @param commit
     * @return intent
     */
    public static Intent createIntent(Repo repository, String commit) {
        return createIntent(repository, commit, null, -1);
    }

    /**
     * Create intent to create a comment on a diff position
     *
     * @param repository
     * @param commit
     * @param path
     * @param position
     * @return intent
     */
    public static Intent createIntent(Repo repository, String commit,
            String path, int position) {
        Builder builder = new Builder("commit.comment.create.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, commit);
        if (isLineComment(path, position))
            builder.add(EXTRA_PATH, path).add(EXTRA_POSITION, position);
        return builder.toIntent();
    }

    private static boolean isLineComment(final String path, final int position) {
        return !TextUtils.isEmpty(path) && position > -1;
    }

    private Repo repository;

    private String commit;

    private int position;

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repository = getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        commit = getStringExtra(EXTRA_BASE);
        position = getIntExtra(EXTRA_POSITION);
        path = getStringExtra(EXTRA_PATH);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.commit_prefix)
                + CommitUtils.abbreviate(commit));
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, repository.owner);
    }

    @Override
    protected void createComment(final String comment) {
        CommitCommentRequest commitComment = new CommitCommentRequest();
        commitComment.body = comment;
        if (isLineComment(path, position)) {
            commitComment.path = path;
            commitComment.position = position;
        }

        new PublishCommitCommentClient(InfoUtils.createCommitInfo(repository, commit),
                commitComment).observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<CommitComment>bindToLifecycle())
                .subscribe(new ProgressObserverAdapter<CommitComment>(this, R.string.creating_comment) {

                    @Override
                    public void onNext(CommitComment created) {
                        super.onNext(created);
                        created.body_html = HtmlUtils.format(created.body_html).toString();
                        finish(created);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ToastUtils.show(CreateCommentActivity.this, e.getMessage());
                    }
                }.start());
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        return new CommentPreviewPagerAdapter(this, repository);
    }
}
