/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.repo;

import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.app.Activity;

import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.user.UserEventMatcher.UserPair;
import com.github.mobile.ui.NewsFragment;
import com.github.mobile.ui.issue.IssuesViewActivity;
import com.github.mobile.ui.user.EventPager;
import com.github.mobile.ui.user.UserViewActivity;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    private Repository repo;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        repo = getSerializableExtra(EXTRA_REPOSITORY);
    }

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            @Override
            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageEvents(repo, page, size);
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
        if (!repo.generateId().equals(repository.generateId()))
            super.viewRepository(repository);
    }

    @Override
    protected void viewIssue(Issue issue, Repository repository) {
        startActivity(IssuesViewActivity.createIntent(issue, repo));
    }

    @Override
    protected boolean viewUser(User user) {
        startActivity(UserViewActivity.createIntent(user));
        return true;
    }

    @Override
    protected void viewUser(UserPair users) {
        if (!viewUser(users.from))
            viewUser(users.to);
    }
}
