package com.github.mobile.android.issue;

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
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

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

    private static final int DEFAULT_SIZE = 20;

    private OnItemClickListener clickListener;

    private LoaderCallbacks<List<Issue>> loadListener;

    @Inject
    private IssueService service;

    private IRepositoryIdProvider repository;

    private IssueFilter filter;

    private Issue lastIssue;

    private boolean hasMore = true;

    private Button moreButton;

    private int pages = 1;

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
        setEmptyText("No Issues");
        moreButton = new Button(getActivity());
        moreButton.setText("Show More...");
        moreButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        moreButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                moreButton.setText("Loading More Issues...");
                pages++;
                lastIssue = (Issue) getListView().getItemAtPosition(getListView().getCount() - 2);
                refresh();
            }
        });
        getListView().addFooterView(moreButton);
    }

    public void onLoaderReset(Loader<List<Issue>> listLoader) {
        super.onLoaderReset(listLoader);

        if (loadListener != null)
            loadListener.onLoaderReset(listLoader);
    }

    public void onLoadFinished(Loader<List<Issue>> loader, final List<Issue> items) {
        super.onLoadFinished(loader, items);

        moreButton.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        moreButton.setText("Show More...");
        if (lastIssue != null) {
            final int target = lastIssue.getNumber();
            getListView().post(new Runnable() {

                public void run() {
                    for (int i = 0; i < items.size(); i++)
                        if (target == items.get(i).getNumber()) {
                            getListView().smoothScrollToPositionFromTop(i, 0);
                            break;
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
                for (Map<String, String> query : filter) {
                    PageIterator<Issue> issues = service.pageIssues(repository, query, 1, DEFAULT_SIZE);
                    for (int i = 0; i < pages && issues.hasNext(); i++)
                        all.addAll(issues.next());
                    hasMore |= issues.hasNext();
                }
                Collections.sort(all, new CreatedAtComparator());
                return all;
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, viewInflatorFor(getActivity(), layout.repo_issue_list_item),
                reflectiveFactoryFor(RepoIssueViewHolder.class));
    }
}
