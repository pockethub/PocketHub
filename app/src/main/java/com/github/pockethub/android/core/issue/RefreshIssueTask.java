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

import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.util.HttpImageGetter;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.issues.IssueCommentService;
import com.meisolsson.githubsdk.service.issues.IssueEventService;
import com.google.inject.Inject;
import com.meisolsson.githubsdk.service.pull_request.PullRequestService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Task to load and store an {@link Issue}
 */
public class RefreshIssueTask implements Observable.OnSubscribe<FullIssue> {

    private static final String TAG = "RefreshIssueTask";

    private final Context context;

    @Inject
    private IssueStore store;

    private final Repository repo;

    private final int issueNumber;

    private final HttpImageGetter bodyImageGetter;

    private final HttpImageGetter commentImageGetter;


    /**
     * Create task to refresh given issue
     *
     * @param repo
     * @param issueNumber
     * @param bodyImageGetter
     */
    public RefreshIssueTask(Context context, Repository repo, int issueNumber, HttpImageGetter bodyImageGetter, HttpImageGetter commentImageGetter) {
        this.repo = repo;
        this.issueNumber = issueNumber;
        this.bodyImageGetter = bodyImageGetter;
        this.context = context;
        this.commentImageGetter = commentImageGetter;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void call(Subscriber<? super FullIssue> subscriber) {
        try {
            Issue issue = store.refreshIssue(repo, issueNumber);

            if (issue.pullRequest() != null) {
                PullRequest pull = getPullRequest(repo.owner().login(), repo.name(), issueNumber);
                issue = issue.toBuilder()
                        .pullRequest(pull)
                        .build();
            }

            bodyImageGetter.encode(issue.id(), issue.bodyHtml());

            List<GitHubComment> comments;
            if(issue.comments() > 0)
                comments = getAllComments(repo.owner().login(), repo.name(), issueNumber);
            else
                comments = Collections.emptyList();

            for (GitHubComment comment : comments)
                commentImageGetter.encode(comment.id(), comment.bodyHtml());

            List<IssueEvent> events = getAllEvents(repo.owner().login(), repo.name(), issueNumber);
            subscriber.onNext(new FullIssue(issue, comments, events));
        } catch (IOException e){
            subscriber.onError(e);
        }
    }

    private List<GitHubComment> getAllComments(String login, String name, int issueNumber) {
        List<GitHubComment> comments = new ArrayList<>();
        int current = 1;
        int last = -1;

        while(current != last) {
            Page<GitHubComment> page = ServiceGenerator.createService(context, IssueCommentService.class)
                    .getIssueComments(login, name, issueNumber, current)
                    .toBlocking()
                    .first();
            comments.addAll(page.items());
            last = page.last() != null ? page.last() : -1;
            current = page.next() != null ? page.next() : -1;
        }

        return comments;
    }

    private List<IssueEvent> getAllEvents(String login, String name, int issueNumber) {
        List<IssueEvent> events = new ArrayList<>();
        int current = 1;
        int last = -1;

        while(current != last) {
            Page<IssueEvent> page = ServiceGenerator.createService(context, IssueEventService.class)
                    .getIssueEvents(login, name, issueNumber, current)
                    .toBlocking()
                    .first();
            events.addAll(page.items());
            last = page.last() != null ? page.last() : -1;
            current = page.next() != null ? page.next() : -1;
        }

        return events;
    }

    private PullRequest getPullRequest(String login, String name, int issueNumber) {
        return ServiceGenerator.createService(context, PullRequestService.class)
                .getPullRequest(login, name, issueNumber)
                .toBlocking()
                .first();
    }
}
