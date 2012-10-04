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
import static com.github.mobile.Intents.EXTRA_PATH;
import static com.github.mobile.Intents.EXTRA_POSITION;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.string;
import com.github.mobile.core.commit.CommitUtils;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Repository;

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
        if (isLineComment(path, position))
            builder.add(EXTRA_PATH, path).add(EXTRA_POSITION, position);
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
        super.onCreate(savedInstanceState);

        repository = getSerializableExtra(EXTRA_REPOSITORY);
        commit = getStringExtra(EXTRA_BASE);
        position = getIntExtra(EXTRA_POSITION);
        path = getStringExtra(EXTRA_PATH);

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
        if (isLineComment(path, position))
            commitComment.setPath(path).setPosition(position);
        new CreateCommentTask(this, repository, commit, commitComment) {

            @Override
            protected void onSuccess(CommitComment comment) throws Exception {
                super.onSuccess(comment);

                finish(comment);
            }

        }.start();
    }
}
