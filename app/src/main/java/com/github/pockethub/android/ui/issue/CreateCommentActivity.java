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

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.service.issues.IssueCommentService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.pockethub.android.Intents.EXTRA_USER;

/**
 * Activity to create a comment on an {@link Issue}
 */
public class CreateCommentActivity extends
        com.github.pockethub.android.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param repoId
     * @param issueNumber
     * @param user
     * @return intent
     */
    public static Intent createIntent(Repository repoId, int issueNumber,
            User user) {
        Builder builder = new Builder("issue.comment.create.VIEW");
        builder.repo(repoId);
        builder.add(EXTRA_ISSUE_NUMBER, issueNumber);
        builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    private Repository repositoryId;

    private int issueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        issueNumber = getIntExtra(EXTRA_ISSUE_NUMBER);
        repositoryId = getParcelableExtra(Intents.EXTRA_REPOSITORY);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.issue_title) + issueNumber);
        actionBar.setSubtitle(InfoUtils.createRepoId(repositoryId));
        avatars.bind(actionBar, (User) getIntent().getParcelableExtra(EXTRA_USER));
    }

    @Override
    protected void createComment(String comment) {
        CommentRequest commentRequest = CommentRequest.builder()
                .body(comment)
                .build();

        ServiceGenerator.createService(this, IssueCommentService.class)
                .createIssueComment(repositoryId.owner().login(), repositoryId.name(), issueNumber, commentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> finish(response.body()));
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        return new CommentPreviewPagerAdapter(this, repositoryId);
    }
}
