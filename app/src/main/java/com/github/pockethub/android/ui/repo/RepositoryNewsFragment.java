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
package com.github.pockethub.android.ui.repo;

import android.content.Context;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.android.ui.NewsFragment;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.ui.user.EventPager;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.service.activity.EventService;

import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    private Repository repo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        repo = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    protected ResourcePager<GitHubEvent> createPager() {
        return new EventPager() {

            @Override
            public PageIterator<GitHubEvent> createIterator(int page, int size) {
                return new PageIterator<>(page1 ->
                        ServiceGenerator.createService(getActivity(), EventService.class)
                                .getRepositoryEvents(repo.owner().login(), repo.name(), page1), page);
            }
        };
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    @Override
    protected void viewRepository(Repository repository) {
        if (!InfoUtils.createRepoId(repo).equals(InfoUtils.createRepoId(repository))) {
            super.viewRepository(repository);
        }
    }

    @Override
    protected void viewIssue(Issue issue, Repository repository) {
        startActivity(IssuesViewActivity.createIntent(issue, repo));
    }

    @Override
    protected boolean viewUser(User user) {
        if (repo.owner().id() != user.id()) {
            startActivity(UserViewActivity.createIntent(user));
            return true;
        }
        return false;
    }

    @Override
    protected void viewUser(UserPair users) {
        if (!viewUser(users.from)) {
            viewUser(users.to);
        }
    }
}
