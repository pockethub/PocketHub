package com.github.mobile.android.issue;

import static com.google.common.collect.Lists.newArrayList;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.async.AuthenticatedUserLoader;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List fragment displaying a list of {@link IssueFilter} items
 */
public class FilterListFragment extends ListLoadingFragment<IssueFilter> {

    @Inject
    private AccountDataManager cache;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_filters));
    }

    @Override
    public Loader<List<IssueFilter>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<IssueFilter>>(getActivity()) {

            public List<IssueFilter> load() {
                List<IssueFilter> filters = newArrayList(cache.getIssueFilters());
                Collections.sort(filters, new Comparator<IssueFilter>() {

                    public int compare(IssueFilter lhs, IssueFilter rhs) {
                        int compare = lhs.getRepository().generateId()
                                .compareToIgnoreCase(rhs.getRepository().generateId());
                        if (compare == 0)
                            compare = lhs.toDisplay().toString().compareToIgnoreCase(rhs.toDisplay().toString());
                        return compare;
                    }
                });
                return filters;
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<IssueFilter> adapterFor(List<IssueFilter> items) {
        return new ViewHoldingListAdapter<IssueFilter>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.issue_filter_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(IssueFilterViewHolder.class));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        IssueFilter filter = (IssueFilter) l.getItemAtPosition(position);
        startActivity(IssueBrowseActivity.createIntent(filter));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
