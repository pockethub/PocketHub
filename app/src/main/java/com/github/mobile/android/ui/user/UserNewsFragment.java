package com.github.mobile.android.ui.user;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.ui.ResourceLoadingIndicator;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
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

    private EventPager pager;

    private ResourceLoadingIndicator loadingIndicator;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingIndicator = new ResourceLoadingIndicator(getActivity(), string.loading_news);
        loadingIndicator.setList(getListView());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pager = new EventPager() {

            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageUserReceivedEvents(user.getLogin(), false, page, size);
            }

            protected Event register(Event resource) {
                return NewsEventViewHolder.isValid(resource) ? resource : null;
            }

        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setFastScrollEnabled(true);
        getListView().setOnScrollListener(new OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!pager.hasMore())
                    return;
                if (getLoaderManager().hasRunningLoaders())
                    return;
                if (getListView().getLastVisiblePosition() >= pager.size())
                    showMore();
            }
        });
    }

    @Override
    public Loader<List<Event>> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<List<Event>>(getActivity(), listItems) {

            public List<Event> loadData() throws IOException {
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Event> adapterFor(List<Event> items) {
        return new ViewHoldingListAdapter<Event>(items, ViewInflator.viewInflatorFor(getActivity(), layout.event_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(NewsEventViewHolder.class));
    }

    @Override
    public void refresh() {
        pager.reset();
        super.refresh();
    }

    /**
     * Show more events while retaining the current {@link EventPager} state
     */
    private void showMore() {
        super.refresh();
    }

    public void onLoadFinished(Loader<List<Event>> loader, List<Event> items) {
        if (pager.hasMore())
            loadingIndicator.showLoading();
        else
            loadingIndicator.setVisible(false);

        super.onLoadFinished(loader, items);
    }
}
