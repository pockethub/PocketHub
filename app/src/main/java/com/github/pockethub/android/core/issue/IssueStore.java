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
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.ItemStore;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.meisolsson.githubsdk.service.pull_request.PullRequestService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import retrofit2.Response;

/**
 * Store of loaded issues
 */
public class IssueStore extends ItemStore {

    private final Map<String, ItemReferences<Issue>> repos = new HashMap<>();

    private final Context context;

    private IssueService service;

    /**
     * Create issue store
     *
     * @param context
     */
    public IssueStore(final Context context) {
        this.context = context;
        service = ServiceGenerator.createService(context, IssueService.class);
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
            if (repo == null) {
                repo = repoFromUrl(issue.htmlUrl());
            }
        }
        return addIssue(repo, issue);
    }

    private Repository repoFromUrl(String url) {
        if (url == null || url.length() == 0) {
            return null;
        }
        String owner = null;
        String name = null;
        for (String segment : url.split("/")) //$NON-NLS-1$
        {
            if (segment.length() > 0) {
                if (owner == null) {
                    owner = segment;
                } else if (name == null) {
                    name = segment;
                } else {
                    break;
                }
            }
        }

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
        if (current != null && current.equals(issue)) {
            return current;
        }

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
     * Refresh issue.
     *
     * @param repository The issues repository
     * @param issueNumber The issue number
     * @return A {@link Single} representing the  issues
     */
    public Single<Issue> refreshIssue(Repository repository, int issueNumber) {
        return service.getIssue(repository.owner().login(), repository.name(), issueNumber)
                .map(response -> addIssueOrThrow(repository, response, R.string.error_issue_load));
    }

    /**
     * Edit issue.
     *
     * @param repository The issues repository
     * @param issueNumber The issues number to change
     * @return A {@link Single} representing the changed issues
     */
    public Single<Issue> editIssue(Repository repository, int issueNumber, IssueRequest request) {
        return service
                .editIssue(repository.owner().login(), repository.name(), issueNumber, request)
                .map(response -> addIssueOrThrow(repository, response, R.string.error_edit_issue));
    }

    /**
     * Change the issue state.
     *
     * @param repository The issues repository
     * @param issueNumber The issue number to change
     * @param state What state to change to
     * @return A {@link Single} representing the changed issue
     */
    public Single<Issue> changeState(Repository repository, int issueNumber, IssueState state) {
        IssueRequest editIssue = IssueRequest.builder()
                .state(state)
                .build();

        return service
                .editIssue(repository.owner().login(), repository.name(), issueNumber, editIssue)
                .map(response -> addIssueOrThrow(repository, response, R.string.error_issue_state));
    }

    /**
     * Adds the issue from the response or throws an error if the request was unsuccessful.
     *
     * @param repository The issues repository
     * @param response The issue response to add
     * @param error String to print if unsuccessful
     * @return The added issue
     */
    private Issue addIssueOrThrow(Repository repository, Response<Issue> response,
                                  @StringRes int error) {
        if (response.isSuccessful()) {
            return addIssue(repository, response.body());
        } else {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            return Issue.builder().build();
        }
    }
}
