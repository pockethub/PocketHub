package com.github.mobile.android.issue;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RequestFuture;
import com.google.inject.Inject;

/**
 * Activity to browse a list of bookmarked {@link IssueFilter} items
 */
public class FilterBrowseActivity extends DialogFragmentActivity implements OnItemLongClickListener {

    private static final String ARG_FILTER = "filter";

    private static final int REQUEST_DELETE = 1;

    @Inject
    private AccountDataManager cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(string.saved_filters_title);
        setContentView(layout.issue_filter_list);

        FilterListFragment filterFragment = (FilterListFragment) getSupportFragmentManager().findFragmentById(
                android.R.id.list);
        filterFragment.getListView().setOnItemLongClickListener(this);
    }

    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (requestCode == REQUEST_DELETE && resultCode == RESULT_OK) {
            IssueFilter filter = (IssueFilter) arguments.getSerializable(ARG_FILTER);
            cache.removeIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    ((FilterListFragment) getSupportFragmentManager().findFragmentById(android.R.id.list)).refresh();
                }
            });
            return;
        }
        super.onDialogResult(requestCode, resultCode, arguments);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        IssueFilter filter = (IssueFilter) parent.getItemAtPosition(position);
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, filter);
        ConfirmDialogFragment.show(this, REQUEST_DELETE, null,
                "Are you sure you want to remove this saved issue filter?", args);
        return true;
    }
}
