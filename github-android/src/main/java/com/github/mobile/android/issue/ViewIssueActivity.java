package com.github.mobile.android.issue;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.R;
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
import com.github.mobile.android.util.GitHubIntents;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.Html;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to view a specific issue
 */
public class ViewIssueActivity extends RoboFragmentActivity {

    /**
     * Create intent to view issue
     *
     * @param issue
     * @return intent
     */
    public static Intent viewIssueIntentFor(Issue issue) {
        return new Builder("repo.issue.VIEW").issue(issue).toIntent();
    }

    @Inject
    private ContextScopedProvider<IssueService> service;

    @InjectView(R.id.list)
    private ListView comments;

    private IssueBodyViewHolder body;

    @InjectExtra(GitHubIntents.EXTRA_REPOSITORY_NAME)
    String repository;
    @InjectExtra(GitHubIntents.EXTRA_REPOSITORY_OWNER)
    String repositoryOwner;
    @InjectExtra(GitHubIntents.EXTRA_ISSUE_NUMBER)
    int issueNumber;

    private HttpImageGetter imageGetter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_view);

        imageGetter = new HttpImageGetter(this);

        ((TextView) findViewById(id.tv_issue_number)).setText("Issue #" + issueNumber);

        loadIssue();
    }

    private void loadIssue() {
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                return service.get(ViewIssueActivity.this).getIssue(repositoryOwner, repository, issueNumber);
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
                loadComments();
            }
        }.execute();
    }

    private void loadComments() {
        new RoboAsyncTask<List<Comment>>(this) {

            public List<Comment> call() throws Exception {
                return service.get(ViewIssueActivity.this).getComments(repositoryOwner, repository, issueNumber);
            }

            protected void onSuccess(List<Comment> issueComments) throws Exception {
                comments.setAdapter(new ViewHoldingListAdapter<Comment>(issueComments, viewInflatorFor(
                        ViewIssueActivity.this, layout.comment_view_item), reflectiveFactoryFor(CommentViewHolder
                        .class, ViewIssueActivity.this, imageGetter)));
            }
        }.execute();
    }
}
