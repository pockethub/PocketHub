package com.github.mobile.android.ui.user;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a news feed for a given user
 */
public class UserNewsFragment extends ListLoadingFragment<Event> {

    @InjectExtra(EXTRA_USER)
    private User user;

    @Inject
    private EventService service;

    @Override
    public Loader<List<Event>> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<List<Event>>(getActivity(), listItems) {

            public List<Event> loadData() throws Exception {
                try {
                    List<Event> events = new ArrayList<Event>(service.pageUserReceivedEvents(user.getLogin(), false)
                            .next());
                    // Remove any events that can't be rendered
                    Iterator<Event> iter = events.iterator();
                    while (iter.hasNext())
                        if (!NewsEventViewHolder.isValid(iter.next()))
                            iter.remove();
                    return events;
                } catch (NoSuchPageException e) {
                    throw e.getCause();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Event> adapterFor(List<Event> items) {
        return new ViewHoldingListAdapter<Event>(items, ViewInflator.viewInflatorFor(getActivity(), layout.event_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(NewsEventViewHolder.class));
    }
}
