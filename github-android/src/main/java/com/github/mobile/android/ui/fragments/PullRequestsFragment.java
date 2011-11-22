package com.github.mobile.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R;
import com.github.mobile.android.views.IssueViewHolder;
import com.github.mobile.android.views.PullRequestViewHolder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.mobile.android.R.layout.issue_list_item;
import static com.github.mobile.android.R.layout.pull_request_list_item;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;

public class PullRequestsFragment extends ListLoadingFragment<PullRequest> {

	private final static String TAG="PullRequestsF";

	@Inject PullRequestService pullRequestService;

	@Override
	protected ListAdapter adapterFor(List<PullRequest> pullRequests) {
		return new ViewHoldingListAdapter<PullRequest>(pullRequests, viewInflatorFor(getActivity(), pull_request_list_item), new ViewHolderFactory<PullRequest>() {
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
