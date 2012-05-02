package com.github.mobile.ui.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.accounts.AuthenticatedUserLoader;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment to display a list of {@link IssueFilter} objects
 */
public class FilterListFragment extends ItemListFragment<IssueFilter> {

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
        setEmptyText(getString(string.no_filters));
    }

    @Override
    public Loader<List<IssueFilter>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<IssueFilter>>(getActivity()) {

            public List<IssueFilter> load() {
                List<IssueFilter> filters = new ArrayList<IssueFilter>(cache.getIssueFilters());
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        IssueFilter filter = (IssueFilter) l.getItemAtPosition(position);
        startActivity(IssueBrowseActivity.createIntent(filter));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    protected ItemListAdapter<IssueFilter, ? extends ItemView> createAdapter(List<IssueFilter> items) {
        return new FilterListAdapter(getActivity().getLayoutInflater(), items.toArray(new IssueFilter[items.size()]),
                avatars);
    }
}