/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.issue;

import static com.github.mobile.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.mobile.Intents.EXTRA_SUBTITLE;
import static com.github.mobile.Intents.EXTRA_USER;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.string;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;

/**
 * Activity to create a comment on an {@link Issue}
 */
public class CreateCommentActivity extends
        com.github.mobile.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param repoId
     * @param issueNumber
     * @param user
     * @return intent
     */
    public static Intent createIntent(RepositoryId repoId, int issueNumber,
            User user) {
        Builder builder = new Builder("issue.comment.create.VIEW");
        builder.repo(repoId);
        builder.add(EXTRA_ISSUE_NUMBER, issueNumber);
        builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    private RepositoryId repositoryId;

    @InjectExtra(EXTRA_ISSUE_NUMBER)
    private int issueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        repositoryId = new RepositoryId(
                intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
                intent.getStringExtra(EXTRA_REPOSITORY_NAME));
        actionBar.setTitle(getString(string.issue_title) + issueNumber);

        actionBar.setSubtitle(intent.getStringExtra(EXTRA_SUBTITLE));
        avatars.bind(actionBar, (User) intent.getSerializableExtra(EXTRA_USER));
    }

    @Override
    protected void createComment(String comment) {
        new CreateCommentTask(this, repositoryId, issueNumber, comment) {

            @Override
            protected void onSuccess(Comment comment) throws Exception {
                super.onSuccess(comment);

                finish(comment);
            }
        }.start();
    }
}
