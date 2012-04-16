package com.github.mobile.android.ui;

import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.core.gist.GistEventMatcher;
import com.github.mobile.android.core.issue.IssueEventMatcher;
import com.github.mobile.android.core.repo.RepositoryEventMatcher;
import com.github.mobile.android.gist.ViewGistsActivity;
import com.github.mobile.android.issue.ViewIssueActivity;
import com.github.mobile.android.ui.repo.RepositoryViewActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.event.Event;

/**
 * Base news fragment class with utilities for subclasses to built on
 */
public abstract class NewsFragment extends PagedListFragment<Event> {

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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) l.getItemAtPosition(position);
        Issue issue = issueMatcher.getIssue(event);
        if (issue != null)
            startActivity(ViewIssueActivity.createIntent(issue));

        Gist gist = gistMatcher.getGist(event);
        if (gist != null)
            startActivity(ViewGistsActivity.createIntent(gist));

        Repository repo = repoMatcher.getRepository(event);
        if (repo != null)
            startActivity(RepositoryViewActivity.createIntent(repo));
    }
}
