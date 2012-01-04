package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a list of Gists
 */
public class IssuesFragment extends ListLoadingFragment<Issue> {

    private OnItemClickListener clickListener;

    @Inject
    private IssueService service;

    private IRepositoryIdProvider repository;

    private IssueFilter filter;

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

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Issues");
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
                final List<Issue> all = new ArrayList<Issue>();
                for (Map<String, String> query : filter)
                    try {
                        all.addAll(service.getIssues(repository, query));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                Collections.sort(all, new CreatedAtComparator());
                return all;
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.repo_issue_list_item), new ViewHolderFactory<Issue>() {

            public ViewHolder<Issue> createViewHolderFor(View view) {
                return new RepoIssueViewHolder(view);
            }
        });
    }
}
