package com.github.mobile.android.issue;

import static com.github.mobile.android.issue.ViewIssueActivity.viewIssueIntentFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.R;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.RequestFuture;
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

/**
 * Activity for browsing a list of issues
 */
public class IssueBrowseActivity extends RoboActivity {

    @InjectView(android.R.id.list)
    private ListView issueList;

    @Inject
    private AccountDataManager cache;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_issue_list);

        final Repository repo = (Repository) getIntent().getSerializableExtra("repository");
        ((TextView) findViewById(id.tv_repo_name)).setText(repo.getName());
        Avatar.bind(this, (ImageView) findViewById(id.iv_gravatar), repo.getOwner());
        loadIssues(repo);

        issueList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View view, int position, long id) {
                Issue issue = (Issue) list.getItemAtPosition(position);
                startActivity(viewIssueIntentFor(issue));
            }
        });
    }

    private void loadIssues(final Repository repo) {
        RequestFuture<List<Issue>> callback = new RequestFuture<List<Issue>>() {

            public void success(List<Issue> issues) {
                issueList.setAdapter(new ViewHoldingListAdapter<Issue>(issues, viewInflatorFor(
                        IssueBrowseActivity.this, layout.repo_issue_list_item), RepoIssueViewHolder.FACTORY));
            }
        };
        Map<String, String> filter = new HashMap<String, String>();
        filter.put(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
        filter.put(IssueService.FIELD_SORT, IssueService.SORT_UPDATED);
        cache.getIssues(repo, filter, callback);
    }
}
