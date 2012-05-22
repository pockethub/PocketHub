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

import static android.content.Intent.ACTION_VIEW;
import static org.eclipse.egit.github.core.event.Event.TYPE_DOWNLOAD;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistEventMatcher;
import com.github.mobile.core.issue.IssueEventMatcher;
import com.github.mobile.core.repo.RepositoryEventMatcher;
import com.github.mobile.core.user.UserEventMatcher;
import com.github.mobile.core.user.UserEventMatcher.UserPair;
import com.github.mobile.ui.gist.ViewGistsActivity;
import com.github.mobile.ui.issue.ViewIssuesActivity;
import com.github.mobile.ui.repo.RepositoryViewActivity;
import com.github.mobile.ui.user.NewsListAdapter;
import com.github.mobile.ui.user.UserViewActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Download;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.DownloadPayload;
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

    /**
     * Matcher for finding a {@link User} from an {@link Event}
     */
    protected final UserEventMatcher userMatcher = new UserEventMatcher();

    @Inject
    private AvatarLoader avatarHelper;

    /**
     * Event service
     */
    @Inject
    protected EventService service;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_news);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) l.getItemAtPosition(position);

        if (TYPE_DOWNLOAD.equals(event.getType())) {
            openDownload(event);
            return;
        }

        Issue issue = issueMatcher.getIssue(event);
        if (issue != null && (issue.getPullRequest() == null || issue.getPullRequest().getHtmlUrl() == null)) {
            startActivity(ViewIssuesActivity.createIntent(issue));
            return;
        }

        Gist gist = gistMatcher.getGist(event);
        if (gist != null) {
            startActivity(ViewGistsActivity.createIntent(gist));
            return;
        }

        Repository repo = repoMatcher.getRepository(event);
        if (repo != null)
            viewRepository(repo);

        UserPair users = userMatcher.getUsers(event);
        if (users != null)
            viewUser(users);
    }

    private void openDownload(Event event) {
        Download download = ((DownloadPayload) event.getPayload()).getDownload();
        if (download == null)
            return;

        String url = download.getHtmlUrl();
        if (TextUtils.isEmpty(url))
            return;

        startActivity(new Intent(ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    protected void viewRepository(Repository repository) {
        startActivity(RepositoryViewActivity.createIntent(repository));
    }

    /**
     * Start an activity to view the given {@link UserPair}
     * <p>
     * This method does nothing by default, subclasses should override
     *
     * @param users
     */
    protected void viewUser(UserPair users) {
    }

    @Override
    protected ItemListAdapter<Event, ? extends ItemView> createAdapter(List<Event> items) {
        return new NewsListAdapter(getActivity().getLayoutInflater(), items.toArray(new Event[items.size()]),
                avatarHelper);
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_news;
    }

    public void onLoadFinished(Loader<List<Event>> loader, List<Event> items) {
        Exception exception = getException(loader);
        if (exception != null) {
            showError(exception, string.error_news_load);
            showList();
            return;
        }

        super.onLoadFinished(loader, items);
    }
}
