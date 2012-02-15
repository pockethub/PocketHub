package com.github.mobile.android.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import android.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.SingleChoiceDialogFragment;
import com.github.mobile.android.comment.CreateCommentActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.Html;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectExtra;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to view a specific issue
 */
public class ViewIssueActivity extends DialogFragmentActivity implements LoaderCallbacks<List<FullIssue>> {

    private static final int REQUEST_CODE_COMMENT = 1;

    private static final int REQUEST_CODE_LABELS = 2;

    private static final int REQUEST_CODE_MILESTONE = 3;

    private static final int REQUEST_CODE_ASSIGNEE = 4;

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

    @Inject
    private IssueStore store;

    @Inject
    private LabelService labelService;

    @Inject
    private MilestoneService milestoneService;

    @Inject
    private CollaboratorService collaboratorService;

    @Inject
    private AvatarHelper avatarHelper;

    private IssueFragment issueFragment;

    private RepositoryId repositoryId;

    @InjectExtra(EXTRA_REPOSITORY_NAME)
    private String repository;

    @InjectExtra(EXTRA_REPOSITORY_OWNER)
    private String repositoryOwner;

    @InjectExtra(value = EXTRA_ISSUE_NUMBER)
    private int issueNumber;

    private Issue issue;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_view);
        setTitle(getString(string.issue_title) + issueNumber);

        repositoryId = RepositoryId.create(repositoryOwner, repository);

        issue = store.getIssue(repositoryId, issueNumber);

        labelsDialog = new LabelsDialog(this, REQUEST_CODE_LABELS, repositoryId, labelService);
        milestoneDialog = new MilestoneDialog(this, REQUEST_CODE_MILESTONE, repositoryId, milestoneService);
        assigneeDialog = new AssigneeDialog(this, REQUEST_CODE_ASSIGNEE, repositoryId, collaboratorService);

        issueFragment = (IssueFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (issueFragment == null) {
            issueFragment = new IssueFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, issueFragment).commit();
        }
        issueFragment.setId(issueNumber).setRepository(new RepositoryId(repositoryOwner, repository))
                .setLoadListener(this);

        if (issue != null)
            displayIssue(issue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(menu.issue_view, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.issue_comment:
            // Don't allow commenting before issue loads
            if (issue != null)
                startActivityForResult(CreateCommentActivity.createIntent(), REQUEST_CODE_COMMENT);
            return true;
        case id.issue_labels:
            if (issue != null)
                labelsDialog.show(issue.getLabels());
            return true;
        case id.issue_milestone:
            if (issue != null)
                milestoneDialog.show(issue.getMilestone());
            return true;
        case id.issue_assignee:
            if (issue != null) {
                User assignee = issue.getAssignee();
                assigneeDialog.show(assignee != null ? assignee.getLogin() : null);
            }
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && REQUEST_CODE_COMMENT == requestCode && data != null) {
            String comment = data.getStringExtra(EXTRA_COMMENT_BODY);
            if (comment != null && comment.length() > 0) {
                createComment(comment);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createComment(final String comment) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Creating comment...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Comment>(this) {

            public Comment call() throws Exception {
                return service.get(ViewIssueActivity.this).createComment(repositoryOwner, repository, issueNumber,
                        comment);
            }

            protected void onSuccess(Comment comment) throws Exception {
                issueFragment.refresh();
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (REQUEST_CODE_LABELS == requestCode && RESULT_OK == resultCode)
            editLabels(arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED));
        if (REQUEST_CODE_MILESTONE == requestCode && RESULT_OK == resultCode)
            editMilestone(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
        if (REQUEST_CODE_ASSIGNEE == requestCode && RESULT_OK == resultCode)
            editAssignee(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
    }

    private void editLabels(final String[] labels) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Updating labels...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                Issue editedIssue = new Issue();
                editedIssue.setNumber(issueNumber);
                List<Label> issueLabels = new ArrayList<Label>(labels.length);
                for (String label : labels)
                    issueLabels.add(new Label().setName(label));
                editedIssue.setLabels(issueLabels);
                return store.editIssue(repositoryId, editedIssue);
            }

            protected void onSuccess(Issue updated) throws Exception {
                issueFragment.updateIssue(updated);
                displayIssue(updated);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
    }

    private void editMilestone(final String title) {
        final int milestoneNumber;
        if (title != null)
            milestoneNumber = milestoneDialog.getMilestoneNumber(title);
        else
            milestoneNumber = -1;

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Updating milestone...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                Issue editedIssue = new Issue();
                editedIssue.setNumber(issueNumber);
                editedIssue.setMilestone(new Milestone().setNumber(milestoneNumber));
                return store.editIssue(repositoryId, editedIssue);
            }

            protected void onSuccess(Issue updated) throws Exception {
                issueFragment.updateIssue(updated);
                displayIssue(updated);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
    }

    private void editAssignee(final String user) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Updating assignee...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                Issue editedIssue = new Issue();
                editedIssue.setAssignee(new User().setLogin(user != null ? user : ""));
                editedIssue.setNumber(issueNumber);
                return store.editIssue(repositoryId, editedIssue);
            }

            protected void onSuccess(Issue updated) throws Exception {
                issueFragment.updateIssue(updated);
                displayIssue(updated);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
    }

    private void displayIssue(Issue issue) {
        ((TextView) findViewById(id.tv_issue_title)).setText(issue.getTitle());
        String reported = "<b>" + issue.getUser().getLogin() + "</b> opened "
                + Time.relativeTimeFor(issue.getCreatedAt());

        TextView creation = (TextView) findViewById(id.tv_issue_creation);
        creation.setText(Html.encode(reported));
        avatarHelper.bind((ImageView) findViewById(id.iv_gravatar), issue.getUser());

        LinearLayout assigneeSection = (LinearLayout) findViewById(id.ll_assignee);
        User assignee = issue.getAssignee();
        if (assignee != null) {
            assigneeSection.setVisibility(VISIBLE);
            ((TextView) assigneeSection.findViewById(id.tv_assignee_name)).setText(Html.encode("<b>"
                    + assignee.getLogin() + "</b> is assigned"));
            avatarHelper.bind((ImageView) assigneeSection.findViewById(id.iv_assignee_gravatar), issue.getUser());
        } else
            assigneeSection.setVisibility(GONE);

        LinearLayout labels = (LinearLayout) findViewById(id.ll_labels);
        if (!issue.getLabels().isEmpty()) {
            labels.setVisibility(VISIBLE);
            LabelsDrawable drawable = new LabelsDrawable(creation.getTextSize(), issue.getLabels());
            drawable.getPaint().setColor(getResources().getColor(android.R.color.transparent));
            labels.setBackgroundDrawable(drawable);
            LayoutParams params = new LayoutParams(drawable.getBounds().width(), drawable.getBounds().height());
            labels.setLayoutParams(params);
        } else
            labels.setVisibility(GONE);

        LinearLayout milestones = (LinearLayout) findViewById(id.ll_milestone);
        if (issue.getMilestone() != null) {
            milestones.setVisibility(VISIBLE);
            ((TextView) milestones.findViewById(id.tv_milestone)).setText(issue.getMilestone().getTitle());
        } else
            milestones.setVisibility(GONE);

    }

    public Loader<List<FullIssue>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    public void onLoadFinished(Loader<List<FullIssue>> loader, List<FullIssue> data) {
        displayIssue(data.get(0).getIssue());
    }

    public void onLoaderReset(Loader<List<FullIssue>> loader) {
    }
}
