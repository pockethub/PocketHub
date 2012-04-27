package com.github.mobile.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.layout;
import com.github.mobile.core.gist.GistEventMatcher;
import com.github.mobile.core.issue.IssueEventMatcher;
import com.github.mobile.core.repo.RepositoryEventMatcher;
import com.github.mobile.gist.ViewGistsActivity;
import com.github.mobile.ui.issue.ViewIssuesActivity;
import com.github.mobile.ui.repo.RepositoryViewActivity;
import com.github.mobile.ui.user.NewsEventViewHolder;
import com.github.mobile.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

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

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) l.getItemAtPosition(position);
        Issue issue = issueMatcher.getIssue(event);
        if (issue != null)
            startActivity(ViewIssuesActivity.createIntent(issue));

        Gist gist = gistMatcher.getGist(event);
        if (gist != null)
            startActivity(ViewGistsActivity.createIntent(gist));

        Repository repo = repoMatcher.getRepository(event);
        if (repo != null)
            startActivity(RepositoryViewActivity.createIntent(repo));
    }

    @Override
    protected ViewHoldingListAdapter<Event> adapterFor(List<Event> items) {
        return new ViewHoldingListAdapter<Event>(items, ViewInflator.viewInflatorFor(getActivity(), layout.event_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(NewsEventViewHolder.class, avatarHelper)) {

            @Override
            public long getItemId(int i) {
                String id = getItem(i).getId();
                return !TextUtils.isEmpty(id) ? id.hashCode() : super.hashCode();
            }
        };
    }
}
