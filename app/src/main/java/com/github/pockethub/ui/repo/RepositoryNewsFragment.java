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
package com.github.pockethub.ui.repo;

import android.content.Context;

import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.repo.GetRepoEventsClient;
import com.github.pockethub.core.PageIterator;
import com.github.pockethub.core.ResourcePager;
import com.github.pockethub.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.ui.NewsFragment;
import com.github.pockethub.ui.issue.IssuesViewActivity;
import com.github.pockethub.ui.user.EventPager;
import com.github.pockethub.ui.user.UserViewActivity;
import com.github.pockethub.util.InfoUtils;

import java.util.List;

import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    private Repo repo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        repo = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    protected ResourcePager<GithubEvent> createPager() {
        return new EventPager() {

            @Override
            public PageIterator<GithubEvent> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<List<GithubEvent>>() {
                    @Override
                    public GithubListClient<List<GithubEvent>> execute(int page) {
                        return new GetRepoEventsClient(InfoUtils.createRepoInfo(repo), page);
                    }
                }, page);
            }
        };
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    @Override
    protected void viewRepository(Repo repository) {
        if (!InfoUtils.createRepoId(repo).equals(InfoUtils.createRepoId(repository)))
            super.viewRepository(repository);
    }

    @Override
    protected void viewIssue(Issue issue, Repo repository) {
        startActivity(IssuesViewActivity.createIntent(issue, repo));
    }

    @Override
    protected boolean viewUser(User user) {
        if (repo.owner.id != user.id) {
            startActivity(UserViewActivity.createIntent(user));
            return true;
        }
        return false;
    }

    @Override
    protected void viewUser(UserPair users) {
        if (!viewUser(users.from))
            viewUser(users.to);
    }
}
