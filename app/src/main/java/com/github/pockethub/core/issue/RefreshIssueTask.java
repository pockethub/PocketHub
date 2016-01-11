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
package com.github.pockethub.core.issue;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.issue.IssueStory;
import com.alorma.github.sdk.bean.issue.PullRequestStory;
import com.alorma.github.sdk.services.issues.story.IssueStoryLoader;
import com.alorma.github.sdk.services.pullrequest.story.PullRequestStoryLoader;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

/**
 * Task to load and store an {@link Issue}
 */
public class RefreshIssueTask extends AuthenticatedUserTask<IssueStory> {

    private static final String TAG = "RefreshIssueTask";

    @Inject
    private IssueStore store;

    private final Repo repo;

    private final int issueNumber;

    private final HttpImageGetter bodyImageGetter;

    private final HttpImageGetter commentImageGetter;

    /**
     * Create task to refresh given issue
     *
     * @param context
     * @param repo
     * @param issueNumber
     * @param bodyImageGetter
     * @param commentImageGetter
     */
    public RefreshIssueTask(Context context,
            Repo repo, int issueNumber,
            HttpImageGetter bodyImageGetter, HttpImageGetter commentImageGetter) {
        super(context);

        this.repo = repo;
        this.issueNumber = issueNumber;
        this.bodyImageGetter = bodyImageGetter;
        this.commentImageGetter = commentImageGetter;
    }

    @Override
    public IssueStory run(Account account) throws Exception {
        Issue issue = store.refreshIssue(repo, issueNumber);
        bodyImageGetter.encode(issue.id, issue.body_html);

        if(issue.pullRequest != null) {
            PullRequestStory story = new PullRequestStoryLoader(InfoUtils.createIssueInfo(repo, issue)).observable().toBlocking().first();
            IssueStory issueStory = new IssueStory();
            issueStory.issue = story.pullRequest;
            issueStory.issue.pullRequest = story.pullRequest;
            issueStory.details = story.details;
            return issueStory;
        }else {
            return new IssueStoryLoader(InfoUtils.createIssueInfo(repo, issue)).observable().toBlocking().first();
        }
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading issue", e);
    }
}
