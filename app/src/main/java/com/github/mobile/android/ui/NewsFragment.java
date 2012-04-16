package com.github.mobile.android.ui;

import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.core.issue.IssueEventMatcher;
import com.github.mobile.android.issue.ViewIssueActivity;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.event.Event;

/**
 * Base news fragment class with utilities for subclasses to built on
 */
public abstract class NewsFragment extends PagedListFragment<Event> {

    /**
     * Matcher for finding an {@link Issue} from an {@link Event}
     */
    protected final IssueEventMatcher issueMatcher = new IssueEventMatcher();

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) l.getItemAtPosition(position);
        Issue issue = issueMatcher.getIssue(event);
        if (issue != null)
            startActivity(ViewIssueActivity.createIntent(issue));
    }
}
