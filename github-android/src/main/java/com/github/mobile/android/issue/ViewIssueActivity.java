package com.github.mobile.android.issue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.Html;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;

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

    @InjectView(id.ll_issue_comments)
    private LinearLayout comments;

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
                loadImages((TextView) findViewById(id.tv_issue_body), issue.getBodyHtml());
                ((TextView) findViewById(id.tv_issue_body)).setText(Html.encode(issue.getBodyHtml()));
                String reported = "<b>" + issue.getUser().getLogin() + "</b> "
                        + Time.relativeTimeFor(issue.getCreatedAt());
                ((TextView) findViewById(id.tv_issue_creation)).setText(Html.encode(reported));
                Avatar.bind(ViewIssueActivity.this, (ImageView) findViewById(id.iv_gravatar), issue.getUser()
                        .getLogin(), issue.getUser().getAvatarUrl());
                loadComments(repository, issue);
            }
        }.execute();
    }

    private void loadImages(final TextView view, final String html) {
        view.setText(Html.encode(html));
        new RoboAsyncTask<CharSequence>(this) {

            public CharSequence call() throws Exception {
                return Html.encode(html, imageGetter);
            }

            protected void onSuccess(CharSequence html) throws Exception {
                view.setText(html);
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
                comments.removeAllViews();
                for (Comment comment : issueComments) {
                    View commentRoot = getLayoutInflater().inflate(layout.gist_view_comment_item, null);
                    final TextView bodyView = (TextView) commentRoot.findViewById(id.tv_gist_comment_body);
                    bodyView.setMovementMethod(LinkMovementMethod.getInstance());
                    loadImages(bodyView, comment.getBodyHtml());
                    final TextView authorView = (TextView) commentRoot.findViewById(id.tv_gist_comment_author);
                    authorView.setText(comment.getUser().getLogin());
                    final TextView dateView = (TextView) commentRoot.findViewById(id.tv_gist_comment_date);
                    dateView.setText(Time.relativeTimeFor(comment.getUpdatedAt()));
                    final ImageView avatarView = (ImageView) commentRoot.findViewById(id.iv_gravatar);
                    Avatar.bind(ViewIssueActivity.this, avatarView, comment.getUser().getLogin(), comment.getUser()
                            .getAvatarUrl());
                    comments.addView(commentRoot);
                }
            }
        }.execute();
    }
}
