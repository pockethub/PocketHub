package com.github.mobile.android.issue;

import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.ui.ResourceLoadingIndicator;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a list of issues
 */
public class IssuesFragment extends ListLoadingFragment<Issue> {

    private OnItemClickListener clickListener;

    private LoaderCallbacks<List<Issue>> loadListener;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private IssueFilter filter;

    private IRepositoryIdProvider repository;

    private boolean hasMore = true;

    private final List<IssuePager> pagers = newArrayList();

    private ResourceLoadingIndicator loadingIndicator;

    @Inject
    private AvatarHelper avatarHelper;

    /**
     * @param repository
     * @return this fragment
     */
    public IssuesFragment setRepository(IRepositoryIdProvider repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param filter
     * @return this fragment
     */
    public IssuesFragment setFilter(IssueFilter filter) {
        this.filter = filter;
        pagers.clear();
        return this;
    }

    /**
     * @param clickListener
     * @return this fragment
     */
    public IssuesFragment setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    /**
     * @param loadListener
     * @return this fragment
     */
    public IssuesFragment setLoadListener(LoaderCallbacks<List<Issue>> loadListener) {
        this.loadListener = loadListener;
        return this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingIndicator = new ResourceLoadingIndicator(getActivity(), string.loading_more_issues);
        loadingIndicator.setList(getListView());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(string.no_issues));
        getListView().setFastScrollEnabled(true);
        getListView().setOnScrollListener(new OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!hasMore)
                    return;
                if (getLoaderManager().hasRunningLoaders())
                    return;
                int size = 0;
                for (IssuePager pager : pagers)
                    size += pager.size();
                if (getListView().getLastVisiblePosition() >= size)
                    showMore();
            }
        });
    }

    public void onLoaderReset(Loader<List<Issue>> listLoader) {
        super.onLoaderReset(listLoader);

        if (loadListener != null)
            loadListener.onLoaderReset(listLoader);
    }

    @Override
    public void refresh() {
        for (IssuePager pager : pagers)
            pager.reset();
        hasMore = true;
        super.refresh();
    }

    /**
     * Show more issues while retaining the current {@link IssuePager} state
     */
    private void showMore() {
        super.refresh();
    }

    public void onLoadFinished(Loader<List<Issue>> loader, final List<Issue> items) {
        if (hasMore)
            loadingIndicator.showLoading();
        else
            loadingIndicator.setVisible(false);

        super.onLoadFinished(loader, items);

        if (loadListener != null)
            loadListener.onLoadFinished(loader, items);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (clickListener != null)
            clickListener.onItemClick(l, v, position, id);
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        // Load pagers if needed
        if (filter != null && pagers.isEmpty())
            for (final Map<String, String> query : filter)
                pagers.add(new IssuePager(store) {

                    public PageIterator<Issue> createIterator(int page, int size) {
                        return service.pageIssues(repository, query, page, size);
                    }
                });
        final IssuePager[] loaderPagers = pagers.toArray(new IssuePager[pagers.size()]);
        return new ThrowableLoader<List<Issue>>(getActivity(), listItems) {

            @Override
            public List<Issue> loadData() throws IOException {
                boolean hasMore = false;
                final List<Issue> all = newArrayList();
                for (IssuePager pager : loaderPagers) {
                    hasMore |= pager.next();
                    all.addAll(pager.getResources());
                }
                Collections.sort(all, new CreatedAtComparator());
                IssuesFragment.this.hasMore = hasMore;
                return all;
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Issue> adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, viewInflatorFor(getActivity(), layout.repo_issue_list_item),
                reflectiveFactoryFor(RepoIssueViewHolder.class, avatarHelper,
                        RepoIssueViewHolder.computeMaxDigits(items)));
    }
}
