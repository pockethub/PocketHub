package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a issue's description and comments
 */
public class IssueFragment extends ListLoadingFragment<Comment> {

    private LoaderCallbacks<List<Comment>> loadListener;

    private IRepositoryIdProvider repository;

    private int id;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private View bodyView;

    @Inject
    private AvatarHelper avatarHelper;

    /**
     * @param id
     * @return this fragment
     */
    public IssueFragment setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Update issue body area with latest values from issue
     *
     * @param issue
     * @return this fragment
     */
    public IssueFragment updateIssue(final Issue issue) {
        if (bodyView != null)
            new IssueHeaderViewHolder(bodyView, avatarHelper, getResources()).updateViewFor(issue);
        return this;
    }

    /**
     * @param repository
     * @return this fragment
     */
    public IssueFragment setRepository(IRepositoryIdProvider repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param loadListener
     * @return this fragment
     */
    public IssueFragment setLoadListener(LoaderCallbacks<List<Comment>> loadListener) {
        this.loadListener = loadListener;
        return this;
    }

    @Override
    public Loader<List<Comment>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<Comment>>(getActivity()) {
            @Override
            public List<Comment> loadInBackground() {
                try {
                    Issue issue = store.refreshIssue(repository, id);
                    List<Comment> comments;
                    if (issue.getComments() > 0)
                        comments = service.getComments(repository, id);
                    else
                        comments = Collections.emptyList();
                    return new FullIssue(issue, comments);
                } catch (IOException e) {
                    showError(e, string.error_issue_load);
                    return new FullIssue();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Comment> adapterFor(List<Comment> items) {
        return new ViewHoldingListAdapter<Comment>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.comment_view_item), ReflectiveHolderFactory.reflectiveFactoryFor(CommentViewHolder.class,
                avatarHelper));
    }

    @Override
    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> items) {
        if (bodyView != null)
            getListView().removeHeaderView(bodyView);

        FullIssue fullIssue = (FullIssue) items;
        Issue issue = fullIssue.getIssue();
        if (issue != null) {
            bodyView = getActivity().getLayoutInflater().inflate(layout.issue_header, null);
            updateIssue(issue);
            getListView().addHeaderView(bodyView);
        }

        super.onLoadFinished(loader, items);
        if (loadListener != null)
            loadListener.onLoadFinished(loader, items);
    }

    @Override
    public void onLoaderReset(Loader<List<Comment>> listLoader) {
        super.onLoaderReset(listLoader);
        if (loadListener != null)
            loadListener.onLoaderReset(listLoader);
    }
}