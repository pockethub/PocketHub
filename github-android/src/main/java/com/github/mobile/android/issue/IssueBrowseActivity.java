package com.github.mobile.android.issue;

import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.util.Avatar;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity for browsing a list of issues
 */
public class IssueBrowseActivity extends RoboActivity {

    @InjectView(android.R.id.list)
    private ListView issueList;

    @Inject
    private IssueService issueService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_issue_list);

        final Repository repo = (Repository) getIntent().getSerializableExtra("repository");
        ((TextView) findViewById(id.tv_repo_name)).setText(repo.getName());
        Avatar.bind(this, (ImageView) findViewById(id.iv_gravatar), repo.getOwner().getLogin(), repo.getOwner()
                .getAvatarUrl());
        loadIssues(repo);
    }

    private void loadIssues(final Repository repo) {
        new RoboAsyncTask<List<Issue>>(this) {

            public List<Issue> call() throws Exception {
                Map<String, String> openFilter = new HashMap<String, String>();
                openFilter.put(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
                openFilter.put(IssueService.FIELD_SORT, IssueService.SORT_UPDATED);
                return issueService.getIssues(repo.getOwner().getLogin(), repo.getName(), openFilter);
            }

            protected void onSuccess(List<Issue> issues) throws Exception {
                issueList.setAdapter(new ViewHoldingListAdapter<Issue>(issues, viewInflatorFor(
                        IssueBrowseActivity.this, layout.repo_issue_list_item), RepoIssueViewHolder.FACTORY));
            };
        }.execute();
    }
}
