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

import android.content.Context;

import com.alorma.github.sdk.bean.dto.request.EditIssueRequestDTO;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.IssueState;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.info.IssueInfo;
import com.alorma.github.sdk.services.issues.ChangeIssueStateClient;
import com.alorma.github.sdk.services.issues.EditIssueClient;
import com.alorma.github.sdk.services.issues.GetIssueClient;
import com.github.pockethub.core.ItemStore;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.InfoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Store of loaded issues
 */
public class IssueStore extends ItemStore {

    //+++
    private final Map<String, ItemReferences<Issue>> repos = new HashMap<>();

    private final Context context;

    /**
     * Create issue store
     *
     * @param context
     */
    public IssueStore(final Context context) {
        this.context = context;
    }

    /**
     * Get issue
     *
     * @param repository
     * @param number
     * @return issue or null if not in store
     */
    public Issue getIssue(Repo repository, int number) {
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
        Repo repo = null;
        if (issue != null) {
            repo = issue.repository;
            if (repo == null)
                repo = repoFromUrl(issue.html_url);
        }
        return addIssue(repo, issue);
    }

    private Repo repoFromUrl(String url){
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

        if(owner != null && owner.length() > 0 && name != null && name.length() > 0){
            Repo repo = new Repo();
            User user = new User();
            user.login = owner;
            repo.owner = user;
            repo.name = name;
            return repo;
        }else{
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
    public Issue addIssue(Repo repository, Issue issue) {
        issue.body_html = (HtmlUtils.format(issue.body_html).toString());
        Issue current = getIssue(repository, issue.number);
        if (current != null) {
            current.assignee = issue.assignee;
            current.body = issue.body;
            current.body_html = issue.body_html;
            current.closedAt = issue.closedAt;
            current.comments = issue.comments;
            current.labels = issue.labels;
            current.milestone = issue.milestone;
            current.pullRequest = issue.pullRequest;
            current.state = issue.state;
            current.title = issue.title;
            current.updated_at = issue.updated_at;
            current.repository = issue.repository;
            return current;
        } else {
            String repoId = InfoUtils.createRepoId(repository);
            ItemReferences<Issue> repoIssues = repos.get(repoId);
            if (repoIssues == null) {
                repoIssues = new ItemReferences<>();
                repos.put(repoId, repoIssues);
            }
            repoIssues.put(issue.number, issue);
            return issue;
        }
    }

    /**
     * Refresh issue
     *
     * @param repository
     * @param number
     * @return refreshed issue
     * @throws IOException
     */
    public Issue refreshIssue(Repo repository, int number) throws IOException {
        IssueInfo issueInfo = InfoUtils.createIssueInfo(repository, number);
        Issue issue = new GetIssueClient(issueInfo).observable().toBlocking().first();
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
    public Issue editIssue(Repo repository, int issueNumber, EditIssueRequestDTO editIssueRequestDTO) throws IOException {
        IssueInfo issueInfo = new IssueInfo(InfoUtils.createRepoInfo(repository));
        issueInfo.num = issueNumber;
        return addIssue(repository, new EditIssueClient(issueInfo, editIssueRequestDTO).observable().toBlocking().first());
    }

    public Issue changeState(Repo repository, int issueNumber, IssueState state) throws IOException {
        IssueInfo issueInfo = new IssueInfo(InfoUtils.createRepoInfo(repository));
        issueInfo.num = issueNumber;
        return addIssue(repository, new ChangeIssueStateClient(issueInfo, state).observable().toBlocking().first());
    }
}
