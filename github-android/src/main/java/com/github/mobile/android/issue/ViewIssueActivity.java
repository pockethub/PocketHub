package com.github.mobile.android.issue;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.Html;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to view a specific issue
 */
public class ViewIssueActivity extends RoboActivity {

    private static final String EXTRA_REPO = "repository";

    private static final String EXTRA_ISSUE = "issue";

    /**
     * Create intent to show issue
     *
     * @param context
     * @param repository
     * @param issue
     * @return intent
     */
    public static Intent createIntent(Context context, Repository repository, Issue issue) {
        Intent intent = new Intent(context, ViewIssueActivity.class);
        intent.putExtra(EXTRA_REPO, repository);
        intent.putExtra(EXTRA_ISSUE, issue);
        return intent;
    }

    @Inject
    private ContextScopedProvider<IssueService> service;

    @InjectView(R.id.list)
    private ListView comments;

    private IssueBodyViewHolder body;

    private HttpImageGetter imageGetter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_view);

        imageGetter = new HttpImageGetter(this);

        Issue issue = (Issue) getIntent().getSerializableExtra(EXTRA_ISSUE);
        Repository repo = (Repository) getIntent().getSerializableExtra(EXTRA_REPO);

        ((TextView) findViewById(id.tv_issue_number)).setText("Issue #" + issue.getNumber());
        loadIssue(repo, issue);
    }

    private void loadIssue(final Repository repository, final Issue issue) {
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                return service.get(ViewIssueActivity.this).getIssue(repository.getOwner().getLogin(),
                        repository.getName(), issue.getNumber());
            }

            protected void onSuccess(Issue issue) throws Exception {
                ((TextView) findViewById(id.tv_issue_title)).setText(issue.getTitle());
                String reported = "<b>" + issue.getUser().getLogin() + "</b> "
                        + Time.relativeTimeFor(issue.getCreatedAt());
                ((TextView) findViewById(id.tv_issue_creation)).setText(Html.encode(reported));
                Avatar.bind(ViewIssueActivity.this, (ImageView) findViewById(id.iv_gravatar), issue.getUser());
                View view = getLayoutInflater().inflate(layout.issue_view_body, null);
                body = new IssueBodyViewHolder(ViewIssueActivity.this, imageGetter, view);
                body.updateViewFor(issue);
                comments.addHeaderView(view);
                loadComments(repository, issue);
            }
        }.execute();
    }

    private void loadComments(final Repository repository, final Issue issue) {
        new RoboAsyncTask<List<Comment>>(this) {

            public List<Comment> call() throws Exception {
                return service.get(ViewIssueActivity.this).getComments(repository.getOwner().getLogin(),
                        repository.getName(), issue.getNumber());
            }

            protected void onSuccess(List<Comment> issueComments) throws Exception {
                comments.setAdapter(new ViewHoldingListAdapter<Comment>(issueComments, ViewInflator.viewInflatorFor(
                        ViewIssueActivity.this, layout.comment_view_item), CommentViewHolder.createFactory(
                        ViewIssueActivity.this, imageGetter)));
            }
        }.execute();
    }
}
