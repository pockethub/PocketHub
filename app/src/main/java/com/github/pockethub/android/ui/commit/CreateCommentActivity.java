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

import com.github.pockethub.android.rx.RxProgress;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.request.repository.CreateCommitComment;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
    public static Intent createIntent(Repository repository, String commit) {
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
    public static Intent createIntent(Repository repository, String commit,
            String path, int position) {
        Builder builder = new Builder("commit.comment.create.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, commit);
        if (isLineComment(path, position)) {
            builder.add(EXTRA_PATH, path).add(EXTRA_POSITION, position);
        }
        return builder.toIntent();
    }

    private static boolean isLineComment(final String path, final int position) {
        return !TextUtils.isEmpty(path) && position > -1;
    }

    private Repository repository;

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
        avatars.bind(actionBar, repository.owner());
    }

    @Override
    protected void createComment(final String comment) {
        CreateCommitComment.Builder commitCommentBuilder = CreateCommitComment.builder()
                .body(comment);


        if(isLineComment(path, position)) {
            commitCommentBuilder.path(path).position(position);
        }

        ServiceGenerator.createService(this, RepositoryCommentService.class)
                .createCommitComment(repository.owner().login(), repository.name(), commit, commitCommentBuilder.build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .compose(RxProgress.bindToLifecycle(this, R.string.creating_comment))
                .subscribe(response -> finish(response.body()),
                        e -> ToastUtils.show(this, e.getMessage()));
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        return new CommentPreviewPagerAdapter(this, repository);
    }
}
