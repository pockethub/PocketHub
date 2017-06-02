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

import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.RxPageUtil;
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

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import roboguice.RoboGuice;

/**
 * Task to load and store an {@link Issue}
 */
public class RefreshIssueTask {

    private static final String TAG = "RefreshIssueTask";

    private final Context context;

    @Inject
    private IssueStore store;

    private final Repository repo;

    private final int issueNumber;

    private final HttpImageGetter bodyImageGetter;

    private final HttpImageGetter commentImageGetter;


    /**
     * Create task to refresh given issue.
     *
     * @param repo The repository to refresh issue from
     * @param issueNumber The issue's number
     * @param bodyImageGetter {@link HttpImageGetter} to fetch images for the bodies
     */
    public RefreshIssueTask(Context context, Repository repo, int issueNumber,
                            HttpImageGetter bodyImageGetter, HttpImageGetter commentImageGetter) {
        this.repo = repo;
        this.issueNumber = issueNumber;
        this.bodyImageGetter = bodyImageGetter;
        this.context = context;
        this.commentImageGetter = commentImageGetter;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    /**
     * Fetches an issue and it's comments, event and pull request if applicable.
     *
     * @return {@link Single} for a {@link FullIssue}
     */
    public Single<FullIssue> refresh() {
        return store.refreshIssue(repo, issueNumber)
                .flatMap(issue -> {
                    if (issue.pullRequest() != null) {
                        return getPullRequest(repo.owner().login(), repo.name(), issueNumber)
                                .map(pullRequest -> issue.toBuilder()
                                        .pullRequest(pullRequest)
                                        .build());
                    }

                    return Single.just(issue);
                })
                .flatMap(issue -> getAllComments(repo.owner().login(), repo.name(), issue)
                        .zipWith(Single.just(issue),
                                (comments, issue1) -> new FullIssue(issue1, comments, null)))
                .zipWith(getAllEvents(repo.owner().login(), repo.name(), issueNumber),
                        (fullIssue, issueEvents) -> new FullIssue(fullIssue.getIssue(),
                                fullIssue.getComments(), issueEvents))
                .map(fullIssue -> {
                    Issue issue = fullIssue.getIssue();
                    bodyImageGetter.encode(issue.id(), issue.bodyHtml());
                    for (GitHubComment comment : fullIssue.getComments()) {
                        commentImageGetter.encode(comment.id(), comment.bodyHtml());
                    }
                    return fullIssue;
                });
    }

    /**
     * Fetches all comments for a given issue.
     *
     * @param login
     * @param name
     * @param issue
     * @return {@link Single}
     */
    private Single<List<GitHubComment>> getAllComments(String login, String name, Issue issue) {
        if (issue.comments() <= 0) {
            return Single.just(Collections.emptyList());
        }

        IssueCommentService service = ServiceGenerator.createService(context,
                IssueCommentService.class);
        return RxPageUtil.getAllPages(page ->
                service.getIssueComments(login, name, issue.number(), page), 1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toList();
    }

    private Single<List<IssueEvent>> getAllEvents(String login, String name, int issueNumber) {
        IssueEventService service = ServiceGenerator
                .createService(context, IssueEventService.class);

        return RxPageUtil.getAllPages(page ->
                service.getIssueEvents(login, name, issueNumber, page), 1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toList();
    }

    private Single<PullRequest> getPullRequest(String login, String name,
                                                         int issueNumber) {
        return ServiceGenerator.createService(context, PullRequestService.class)
                .getPullRequest(login, name, issueNumber)
                .map(Response::body);
    }


}
