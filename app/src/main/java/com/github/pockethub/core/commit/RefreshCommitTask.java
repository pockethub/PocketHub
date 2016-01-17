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
package com.github.pockethub.core.commit;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.GitCommit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.commit.GetCommitCommentsClient;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

import java.util.List;

/**
 * Task to load a commit by SHA-1 id
 */
public class RefreshCommitTask extends AuthenticatedUserTask<FullCommit> {

    private static final String TAG = "RefreshCommitTask";

    @Inject
    private CommitStore store;

    private final Repo repository;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * @param context
     * @param repository
     * @param id
     * @param imageGetter
     */
    public RefreshCommitTask(Context context, Repo repository,
            String id, HttpImageGetter imageGetter) {
        super(context);

        this.repository = repository;
        this.id = id;
        this.imageGetter = imageGetter;
    }

    @Override
    protected FullCommit run(Account account) throws Exception {
        Commit commit = store.refreshCommit(repository, id);
        GitCommit rawCommit = commit.commit;
        if (rawCommit != null && rawCommit.comment_count > 0) {
            List<CommitComment> comments = new GetCommitCommentsClient(InfoUtils.createCommitInfo(repository, commit.sha))
                    .observable().toBlocking().first().first;
            for (CommitComment comment : comments) {
                String formatted = HtmlUtils.format(comment.body_html).toString();
                comment.body_html = formatted;
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
