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
package com.github.mobile.core.issue;

import android.content.Context;
import android.util.Log;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.util.HtmlUtils;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Task to load and store an {@link Issue}
 */
public class RefreshIssueTask extends AuthenticatedUserTask<FullIssue> {

    private static final String TAG = "RefreshIssueTask";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    /**
     * Create task to refresh given issue
     *
     * @param context
     * @param repositoryId
     * @param issueNumber
     */
    public RefreshIssueTask(Context context, IRepositoryIdProvider repositoryId, int issueNumber) {
        super(context);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
    }

    @Override
    public FullIssue run() throws Exception {
        Issue issue = store.refreshIssue(repositoryId, issueNumber);
        List<Comment> comments;
        if (issue.getComments() > 0)
            comments = service.getComments(repositoryId, issueNumber);
        else
            comments = Collections.emptyList();
        for (Comment comment : comments)
            comment.setBodyHtml(HtmlUtils.format(comment.getBodyHtml()).toString());
        return new FullIssue(issue, comments);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading issue", e);
    }
}
