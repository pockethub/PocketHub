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
package com.github.mobile.ui;

import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistEventMatcher;
import com.github.mobile.core.issue.IssueEventMatcher;
import com.github.mobile.core.repo.RepositoryEventMatcher;
import com.github.mobile.ui.gist.ViewGistsActivity;
import com.github.mobile.ui.issue.ViewIssuesActivity;
import com.github.mobile.ui.repo.RepositoryViewActivity;
import com.github.mobile.ui.user.NewsEventListAdapter;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

/**
 * Base news fragment class with utilities for subclasses to built on
 */
public abstract class NewsFragment extends PagedItemFragment<Event> {

    /**
     * Matcher for finding an {@link Issue} from an {@link Event}
     */
    protected final IssueEventMatcher issueMatcher = new IssueEventMatcher();

    /**
     * Matcher for finding a {@link Gist} from an {@link Event}
     */
    protected final GistEventMatcher gistMatcher = new GistEventMatcher();

    /**
     * Matcher for finding a {@link Repository} from an {@link Event}
     */
    protected final RepositoryEventMatcher repoMatcher = new RepositoryEventMatcher();

    @Inject
    private AvatarLoader avatarHelper;

    /**
     * Event service
     */
    @Inject
    protected EventService service;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) l.getItemAtPosition(position);
        Issue issue = issueMatcher.getIssue(event);
        if (issue != null && (issue.getPullRequest() == null || issue.getPullRequest().getHtmlUrl() == null))
            startActivity(ViewIssuesActivity.createIntent(issue));

        Gist gist = gistMatcher.getGist(event);
        if (gist != null)
            startActivity(ViewGistsActivity.createIntent(gist));

        Repository repo = repoMatcher.getRepository(event);
        if (repo != null)
            viewRepository(repo);
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    protected void viewRepository(Repository repository) {
        startActivity(RepositoryViewActivity.createIntent(repository));
    }

    @Override
    protected ItemListAdapter<Event, ? extends ItemView> createAdapter(List<Event> items) {
        return new NewsEventListAdapter(getActivity().getLayoutInflater(), items.toArray(new Event[items.size()]),
                avatarHelper);
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_news;
    }
}
