package com.github.mobile.android.ui.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import android.os.Bundle;

import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.ui.NewsFragment;
import com.github.mobile.android.ui.user.EventPager;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectExtra;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    @Inject
    private ContextScopedProvider<EventService> serviceProvider;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewHelper.configure(getActivity(), getListView(), true);
    }

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            public PageIterator<Event> createIterator(int page, int size) {
                return serviceProvider.get(getActivity()).pageEvents(repo, page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_news;
    }
}
