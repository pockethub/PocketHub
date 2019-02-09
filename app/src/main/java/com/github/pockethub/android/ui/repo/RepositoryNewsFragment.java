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
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.android.ui.NewsFragment;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.service.activity.EventService;

import io.reactivex.Single;
import retrofit2.Response;

import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    EventService service = ServiceGenerator.createService(getActivity(), EventService.class);

    private Repository repo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        repo = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    protected Single<Response<Page<GitHubEvent>>> loadData(int page) {
        return service.getRepositoryEvents(repo.owner().login(), repo.name(), page);
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
        startActivity(IssuesViewActivity.Companion.createIntent(issue, repo));
    }

    @Override
    protected boolean viewUser(User user) {
        if (repo.owner().id() != user.id()) {
            startActivity(UserViewActivity.Companion.createIntent(user));
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
