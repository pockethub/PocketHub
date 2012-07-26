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
package com.github.mobile.ui.commit;

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.string;
import com.github.mobile.core.commit.CommitUtils;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

/**
 * Activity to create a comment on a commit
 */
public class CreateCommentActivity extends
        com.github.mobile.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param repository
     * @param commit
     * @return intent
     */
    public static Intent createIntent(Repository repository, String commit) {
        Builder builder = new Builder("commit.comment.create.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, commit);
        return builder.toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @InjectExtra(EXTRA_BASE)
    private String commit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(string.commit_prefix)
                + CommitUtils.abbreviate(commit));
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());
    }

    @Override
    protected void createComment(String comment) {
        CommitComment commitComment = new CommitComment();
        commitComment.setBody(comment);
        new CreateCommentTask(this, repository, commit, commitComment) {

            @Override
            protected void onSuccess(CommitComment comment) throws Exception {
                super.onSuccess(comment);

                finish(comment);
            }

        }.start();
    }
}
