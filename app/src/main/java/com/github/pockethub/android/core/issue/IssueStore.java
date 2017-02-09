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

import com.github.pockethub.android.core.ItemStore;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.meisolsson.githubsdk.service.pull_request.PullRequestService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Store of loaded issues
 */
public class IssueStore extends ItemStore {

    //+++
    private final Map<String, ItemReferences<Issue>> repos = new HashMap<>();

    private IssueService service;

    private PullRequestService pullRequestService;

    /**
     * Create issue store
     *
     * @param context
     */
    public IssueStore(final Context context) {
        service = ServiceGenerator.createService(context, IssueService.class);
        pullRequestService = ServiceGenerator.createService(context, PullRequestService.class);
    }

    /**
     * Get issue
     *
     * @param repository
     * @param number
     * @return issue or null if not in store
     */
    public Issue getIssue(Repository repository, int number) {
        ItemReferences<Issue> repoIssues = repos.get(InfoUtils.createRepoId(repository));
        return repoIssues != null ? repoIssues.get(number) : null;
    }

    /**
     * Add issue to store
     *
     * @param issue
     * @return issue
     */
    public Issue addIssue(Issue issue) {
        Repository repo = null;
        if (issue != null) {
            repo = issue.repository();
            if (repo == null)
                repo = repoFromUrl(issue.htmlUrl());
        }
        return addIssue(repo, issue);
    }

    private Repository repoFromUrl(String url) {
        if (url == null || url.length() == 0)
            return null;
        String owner = null;
        String name = null;
        for (String segment : url.split("/")) //$NON-NLS-1$
            if (segment.length() > 0)
                if (owner == null)
                    owner = segment;
                else if (name == null)
                    name = segment;
                else
                    break;

        if (owner != null && owner.length() > 0 && name != null && name.length() > 0) {
            return InfoUtils.createRepoFromData(owner, name);
        } else {
            return null;
        }
    }

    /**
     * Add issue to store
     *
     * @param repository
     * @param issue
     * @return issue
     */
    public Issue addIssue(Repository repository, Issue issue) {
        Issue current = getIssue(repository, issue.number());
        if (current != null && current.equals(issue))
            return current;

        String repoId = InfoUtils.createRepoId(repository);
        ItemReferences<Issue> repoIssues = repos.get(repoId);
        if (repoIssues == null) {
            repoIssues = new ItemReferences<>();
            repos.put(repoId, repoIssues);
        }

        repoIssues.put(issue.number(), issue);
        return issue;
    }

    /**
     * Refresh issue
     *
     * @param repository
     * @param number
     * @return refreshed issue
     * @throws IOException
     */
    public Issue refreshIssue(Repository repository, int number) throws IOException {
        Issue issue = service.getIssue(repository.owner().login(), repository.name(), number)
                .toBlocking()
                .first();
        return addIssue(repository, issue);
    }

    /**
     * Edit issue
     *
     * @param repository
     * @param issueNumber
     * @return edited issue
     * @throws IOException
     */
    public Issue editIssue(Repository repository, int issueNumber, IssueRequest request) throws IOException {
        Issue issue = service.editIssue(repository.owner().login(), repository.name(), issueNumber, request)
                .toBlocking()
                .first();
        return addIssue(repository, issue);
    }

    public Issue changeState(Repository repository, int issueNumber, IssueState state) throws IOException {
        IssueRequest editIssue = IssueRequest.builder()
                .state(state)
                .build();
        Issue issue = service.editIssue(repository.owner().login(), repository.name(), issueNumber, editIssue)
                .toBlocking()
                .first();
        return addIssue(repository, issue);
    }
}
