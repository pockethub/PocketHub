package com.github.mobile.android.issue;

import static org.eclipse.egit.github.core.service.IssueService.DIRECTION_DESCENDING;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_DIRECTION;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_FILTER;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_SORT;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_ASSIGNED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_CREATED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_MENTIONED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_SUBSCRIBED;
import static org.eclipse.egit.github.core.service.IssueService.SORT_UPDATED;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;
import com.viewpagerindicator.TitleProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Pager adapter for the issues dashboard
 */
public class IssueDashboardPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    private static final String TAG = "ISPA";

    private final Context context;

    private final IssueService service;

    /**
     * Create pager adapter
     *
     * @param context
     * @param service
     * @param fragmentManager
     */
    public IssueDashboardPagerAdapter(Context context, IssueService service, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
        this.service = service;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        String filter = null;
        switch (position) {
        case 0:
            filter = FILTER_SUBSCRIBED;
            break;
        case 1:
            filter = FILTER_ASSIGNED;
            break;
        case 2:
            filter = FILTER_CREATED;
            break;
        case 3:
            filter = FILTER_MENTIONED;
            break;
        default:
            return null;
        }
        final Map<String, String> filterData = new HashMap<String, String>();
        filterData.put(FIELD_FILTER, filter);
        filterData.put(FIELD_SORT, SORT_UPDATED);
        filterData.put(FIELD_DIRECTION, DIRECTION_DESCENDING);
        return new ListLoadingFragment<Issue>() {

            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getListView().setFastScrollEnabled(true);
            }

            @Override
            public Loader<List<Issue>> onCreateLoader(int id, Bundle args) {
                return new AsyncLoader<List<Issue>>(getActivity()) {

                    public List<Issue> loadInBackground() {
                        try {
                            return service.getIssues(filterData);
                        } catch (IOException e) {
                            Log.d(TAG, "Exception getting issues", e);
                        }
                        return Collections.emptyList();
                    }
                };
            }

            @Override
            protected ListAdapter adapterFor(List<Issue> items) {
                return new ViewHoldingListAdapter<Issue>(items, ViewInflator.viewInflatorFor(context,
                        layout.dashboard_issue_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                        DashboardIssueViewHolder.class, RepoIssueViewHolder.computeMaxDigits(items)));
            }

            @Override
            public void onListItemClick(ListView l, View v, int position, long id) {
                Issue issue = (Issue) l.getItemAtPosition(position);
                startActivity(ViewIssueActivity.viewIssueIntentFor(issue));
            }
        };
    }

    @Override
    public String getTitle(int position) {
        switch (position) {
        case 0:
            return context.getResources().getString(string.dasbhoard_watched);
        case 1:
            return context.getResources().getString(string.dasbhoard_assigned);
        case 2:
            return context.getResources().getString(string.dasbhoard_created);
        case 3:
            return context.getResources().getString(string.dasbhoard_mentioned);
        default:
            return null;
        }
    }
}
