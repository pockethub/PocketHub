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
package com.github.pockethub.android.core.issue;

import android.content.Context;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.issue.IssueStory;
import com.alorma.github.sdk.bean.issue.PullRequestStory;
import com.alorma.github.sdk.services.issues.story.IssueStoryLoader;
import com.alorma.github.sdk.services.pullrequest.story.PullRequestStoryLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
import com.google.inject.Inject;

import java.io.IOException;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

/**
 * Task to load and store an {@link Issue}
 */
public class RefreshIssueTask implements Observable.OnSubscribe<IssueStory> {

    private static final String TAG = "RefreshIssueTask";

    @Inject
    private IssueStore store;

    private final Repo repo;

    private final int issueNumber;

    private final HttpImageGetter bodyImageGetter;


    /**
     * Create task to refresh given issue
     *
     * @param repo
     * @param issueNumber
     * @param bodyImageGetter
     */
    public RefreshIssueTask(Context context, Repo repo, int issueNumber, HttpImageGetter bodyImageGetter) {
        this.repo = repo;
        this.issueNumber = issueNumber;
        this.bodyImageGetter = bodyImageGetter;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void call(Subscriber<? super IssueStory> subscriber) {
        try {
            Issue issue = store.refreshIssue(repo, issueNumber);
            bodyImageGetter.encode(issue.id, issue.body_html);

            if (issue.pullRequest != null) {
                PullRequestStory story = new PullRequestStoryLoader(InfoUtils.createIssueInfo(repo, issue)).observable().toBlocking().first();
                IssueStory issueStory = new IssueStory();
                issueStory.issue = story.pullRequest;
                issueStory.issue.pullRequest = story.pullRequest;
                issueStory.details = story.details;
                subscriber.onNext(issueStory);
            } else {
                subscriber.onNext(new IssueStoryLoader(InfoUtils.createIssueInfo(repo, issue)).observable().toBlocking().first());
            }
        } catch (IOException e){
            subscriber.onError(e);
        }
    }
}
