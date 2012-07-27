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
package com.github.mobile.core.commit;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.util.HtmlUtils;
import com.github.mobile.util.HttpImageGetter;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

/**
 * Task to load a commit by SHA-1 id
 */
public class RefreshCommitTask extends AuthenticatedUserTask<FullCommit> {

    private static final String TAG = "RefreshCommitTask";

    @Inject
    private CommitService service;

    private final IRepositoryIdProvider repository;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * @param context
     * @param repository
     * @param id
     * @param imageGetter
     */
    public RefreshCommitTask(Context context, IRepositoryIdProvider repository,
            String id, HttpImageGetter imageGetter) {
        super(context);

        this.repository = repository;
        this.id = id;
        this.imageGetter = imageGetter;
    }

    @Override
    protected FullCommit run(Account account) throws Exception {
        RepositoryCommit commit = service.getCommit(repository, id);
        Commit rawCommit = commit.getCommit();
        if (rawCommit != null && rawCommit.getCommentCount() > 0) {
            List<CommitComment> comments = service.getComments(repository,
                    commit.getSha());
            for (CommitComment comment : comments) {
                String formatted = HtmlUtils.format(comment.getBodyHtml())
                        .toString();
                comment.setBodyHtml(formatted);
                imageGetter.encode(comment, formatted);
            }
            return new FullCommit(commit, comments);
        } else
            return new FullCommit(commit);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading commit", e);
    }
}
