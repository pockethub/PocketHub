package com.github.mobile.android.issue;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.ArrayList;
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

    private IRepositoryIdProvider repository;

    private Issue lastIssue;

    private boolean hasMore = true;

    private Button moreButton;

    private final List<IssuePager> pagers = new ArrayList<IssuePager>();

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
        pagers.clear();
        for (final Map<String, String> query : filter)
            pagers.add(new IssuePager() {

                public PageIterator<Issue> createIterator(int page, int size) {
                    return service.pageIssues(repository, query, page, size);
                }
            });
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

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_issues));
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
        if (hasMore) {
            if (moreButton == null) {
                moreButton = new Button(getActivity());
                moreButton.setLayoutParams(new LayoutParams(FILL_PARENT, WRAP_CONTENT));
                moreButton.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        moreButton.setText(getString(string.loading_more_issues));
                        moreButton.setEnabled(false);
                        lastIssue = (Issue) getListView().getItemAtPosition(
                                getListView().getCount() - getListView().getFooterViewsCount() - 1);
                        showMore();
                    }
                });
                getListView().addFooterView(moreButton);
            }
            moreButton.setEnabled(true);
            moreButton.setText(getString(string.show_more));
        } else {
            getListView().removeFooterView(moreButton);
            moreButton = null;
        }

        super.onLoadFinished(loader, items);

        if (lastIssue != null) {
            final int target = lastIssue.getNumber();
            getListView().post(new Runnable() {

                public void run() {
                    ListView view = getListView();
                    for (int i = 0; i < view.getCount() - view.getFooterViewsCount(); i++)
                        if (target == ((Issue) view.getItemAtPosition(i)).getNumber()) {
                            view.setSelection(i);
                            return;
                        }
                }
            });
        }

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
        return new AsyncLoader<List<Issue>>(getActivity()) {

            @Override
            public List<Issue> loadInBackground() {
                hasMore = false;
                final List<Issue> all = new ArrayList<Issue>();
                boolean error = false;
                for (IssuePager pager : pagers) {
                    try {
                        if (!error)
                            hasMore |= pager.next();
                    } catch (final IOException e) {
                        if (!error)
                            showError(e, string.error_issues_load);
                        error = true;
                    }
                    all.addAll(pager.getIssues());
                }
                Collections.sort(all, new CreatedAtComparator());
                return all;
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, viewInflatorFor(getActivity(), layout.repo_issue_list_item),
                reflectiveFactoryFor(RepoIssueViewHolder.class, RepoIssueViewHolder.computeMaxDigits(items)));
    }
}
