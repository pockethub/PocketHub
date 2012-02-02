package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.ArrayList;
import java.util.List;

/**
 * List fragment displaying a list of {@link IssueFilter} items
 */
public class FilterListFragment extends ListLoadingFragment<IssueFilter> {

    @Inject
    private AccountDataManager cache;

    public Loader<List<IssueFilter>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<IssueFilter>>(getActivity()) {

            public List<IssueFilter> loadInBackground() {
                return new ArrayList<IssueFilter>(cache.getIssueFilters());
            }
        };
    }

    protected ListAdapter adapterFor(List<IssueFilter> items) {
        return new ViewHoldingListAdapter<IssueFilter>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.issue_filter_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(IssueFilterViewHolder.class));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        IssueFilter filter = (IssueFilter) l.getItemAtPosition(position);
        startActivity(IssueBrowseActivity.createIntent(filter));
    }
}
