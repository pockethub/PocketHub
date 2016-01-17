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
package com.github.pockethub.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.issues.NewIssueCommentClient;
import com.github.pockethub.Intents;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.rx.ObserverAdapter;
import com.github.pockethub.ui.comment.CommentPreviewPagerAdapter;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.InfoUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.pockethub.Intents.EXTRA_USER;

/**
 * Activity to create a comment on an {@link Issue}
 */
public class CreateCommentActivity extends
        com.github.pockethub.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param repoId
     * @param issueNumber
     * @param user
     * @return intent
     */
    public static Intent createIntent(Repo repoId, int issueNumber,
            User user) {
        Builder builder = new Builder("issue.comment.create.VIEW");
        builder.repo(repoId);
        builder.add(EXTRA_ISSUE_NUMBER, issueNumber);
        builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    private Repo repositoryId;

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
        new NewIssueCommentClient(InfoUtils.createIssueInfo(repositoryId, issueNumber), comment)
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GithubComment>bindToLifecycle())
                .subscribe(new ObserverAdapter<GithubComment>() {
                    @Override
                    public void onNext(GithubComment githubComment) {
                        githubComment.body_html = HtmlUtils.format(githubComment.body_html).toString();
                        finish(githubComment);
                    }
                });
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        return new CommentPreviewPagerAdapter(this, repositoryId);
    }
}
