package com.github.mobile.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R;
import com.github.mobile.android.views.IssueViewHolder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.mobile.android.R.layout.issue_list_item;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;

public class IssuesFragment extends ListLoadingFragment<Issue> {

	private final static String TAG="IssuesF";

	@Inject	IssueService issueService;

	@Override
	ListAdapter adapterFor(List<Issue> issues) {
		return new ViewHoldingListAdapter<Issue>(issues, viewInflatorFor(getActivity(), issue_list_item), new ViewHolderFactory<Issue>() {
			public ViewHolder<Issue> createViewHolderFor(View view) {
				return new IssueViewHolder(view);
			}
		});
	}

	@Override
	public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
		return new AsyncLoader<List<Issue>>(getActivity()) {
			@Override
			public List<Issue> loadInBackground() {
				Log.i(TAG, "started loadInBackground");
				try {
					return issueService.getIssues("rtyley","agit", Collections.<String, String>emptyMap());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

}
