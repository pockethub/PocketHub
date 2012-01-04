package com.github.mobile.android.ui.fragments;

import static com.github.mobile.android.R.layout.pull_request_list_item;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.views.PullRequestViewHolder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;

public class PullRequestsFragment extends ListLoadingFragment<PullRequest> {

    private final static String TAG = "PullRequestsF";

    @Inject
    PullRequestService pullRequestService;

    @Override
    protected ListAdapter adapterFor(List<PullRequest> pullRequests) {
        return new ViewHoldingListAdapter<PullRequest>(pullRequests, viewInflatorFor(getActivity(),
                pull_request_list_item), reflectiveFactoryFor(PullRequestViewHolder.class));
    }

    @Override
    public Loader<List<PullRequest>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<PullRequest>>(getActivity()) {
            @Override
            public List<PullRequest> loadInBackground() {
                Log.i(TAG, "started loadInBackground");
                try {
                    return pullRequestService.getPullRequests(RepositoryId.createFromId("rtyley/agit"), null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
