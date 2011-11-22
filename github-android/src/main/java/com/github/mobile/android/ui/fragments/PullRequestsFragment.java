package com.github.mobile.android.ui.fragments;

import static com.github.mobile.android.R.layout.pull_request_list_item;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.views.PullRequestViewHolder;
import com.google.inject.Inject;

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
                pull_request_list_item), new ViewHolderFactory<PullRequest>() {
            public ViewHolder<PullRequest> createViewHolderFor(View view) {
                return new PullRequestViewHolder(view);
            }
        });
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
