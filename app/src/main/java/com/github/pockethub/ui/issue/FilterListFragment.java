package com.github.pockethub.ui.issue;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.AsyncLoader;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.persistence.AccountDataManager;
import com.github.pockethub.ui.ItemListFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment to display a list of {@link IssueFilter} objects
 */
public class FilterListFragment extends ItemListFragment<IssueFilter> implements
        Comparator<IssueFilter> {

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_bookmarks);
    }

    @Override
    public Loader<List<IssueFilter>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<IssueFilter>>(getActivity()) {

            @Override
            public List<IssueFilter> loadInBackground() {
                List<IssueFilter> filters = new ArrayList<>(
                        cache.getIssueFilters());
                Collections.sort(filters, FilterListFragment.this);
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
    protected int getErrorMessage(Exception exception) {
        return R.string.error_bookmarks_load;
    }

    @Override
    protected SingleTypeAdapter<IssueFilter> createAdapter(
            List<IssueFilter> items) {
        return new FilterListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new IssueFilter[items.size()]), avatars);
    }

    @Override
    public int compare(final IssueFilter lhs, final IssueFilter rhs) {
        int compare = CASE_INSENSITIVE_ORDER.compare(lhs.getRepository()
                .generateId(), rhs.getRepository().generateId());
        if (compare == 0)
            compare = CASE_INSENSITIVE_ORDER.compare(
                    lhs.toDisplay().toString(), rhs.toDisplay().toString());
        return compare;
    }
}
