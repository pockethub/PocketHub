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

package com.github.pockethub.util;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.info.CommitInfo;
import com.alorma.github.sdk.bean.info.IssueInfo;
import com.alorma.github.sdk.bean.info.RepoInfo;

public class InfoUtils {

    public static RepoInfo createRepoInfo(Repo repo) {
        return createRepoInfo(repo, repo.default_branch);
    }

    public static RepoInfo createRepoInfo(Repo repo, String branch) {
        RepoInfo repoInfo = new RepoInfo();
        repoInfo.permissions = repo.permissions;
        repoInfo.branch = branch;
        repoInfo.name = repo.name;
        repoInfo.owner = repo.owner.login;
        return repoInfo;
    }

    public static IssueInfo createIssueInfo(Repo repo, Issue issue) {
        IssueInfo issueInfo = new IssueInfo(createRepoInfo(repo));
        if (issue != null) {
            issueInfo.num = issue.number;
            issueInfo.state = issue.state;
            issueInfo.commentNum = issue.comments;
        }
        return issueInfo;
    }

    public static IssueInfo createIssueInfo(Repo repo, int issueNumber) {
        IssueInfo issueInfo = new IssueInfo(createRepoInfo(repo));
        issueInfo.num = issueNumber;
        return issueInfo;
    }

    public static Repo createRepoFromUrl(String url) {
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
            Repo repo = new Repo();
            User user = new User();
            user.login = owner;
            repo.owner = user;
            repo.name = name;
            return repo;
        } else {
            return null;
        }
    }

    public static String createRepoId(Repo repo) {
        if(repo.name.contains("/"))
            return repo.name;
        else
            return createRepoId(repo.owner.login, repo.name);
    }

    public static String createRepoId(String owner, String name) {
        return owner + "/" + name;
    }

    public static Repo createRepoFromData(String repoOwner, String repoName) {
        Repo repo = new Repo();
        User user = new User();
        user.login = repoOwner;
        repo.owner = user;
        repo.name = repoName;
        return repo;
    }

    public static CommitInfo createCommitInfo(Repo repo, String sha) {
        CommitInfo commitInfo = new CommitInfo();
        commitInfo.repoInfo = createRepoInfo(repo);
        commitInfo.sha = sha;
        return commitInfo;
    }
}
