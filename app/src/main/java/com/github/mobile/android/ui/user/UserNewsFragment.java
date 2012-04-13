package com.github.mobile.android.ui.user;

import static com.github.mobile.android.HomeActivity.OrgSelectionListener;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.app.Activity;
import android.os.Bundle;

import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.ui.PagedListFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

/**
 * Fragment to display a news feed for a given user/org
 */
public class UserNewsFragment extends PagedListFragment<Event> implements OrgSelectionListener {

    private User org;

    @Inject
    private EventService service;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).registerOrgSelectionListener(this);
    }

    @Override
    public void onOrgSelected(User org) {
        this.org = org;
        hideOldContentAndRefresh();
    }

    @Override
    protected ViewHoldingListAdapter<Event> adapterFor(List<Event> items) {
        return new ViewHoldingListAdapter<Event>(items, viewInflatorFor(getActivity(), layout.event_item),
            reflectiveFactoryFor(NewsEventViewHolder.class));
    }

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageUserReceivedEvents(org.getLogin(), false, page, size);
            }

            protected Event register(Event resource) {
                return NewsEventViewHolder.isValid(resource) ? resource : null;
            }

        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_news;
    }

}
