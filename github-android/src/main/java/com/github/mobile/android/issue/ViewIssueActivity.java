package com.github.mobile.android.issue;

import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.SingleChoiceDialogFragment;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.comment.CreateCommentActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ErrorHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to view a specific issue
 */
public class ViewIssueActivity extends DialogFragmentActivity {

    private static final String ARG_COMMENTS = "comments";

    private static final int REQUEST_CODE_COMMENT = 1;

    private static final int REQUEST_CODE_LABELS = 2;

    private static final int REQUEST_CODE_MILESTONE = 3;

    private static final int REQUEST_CODE_ASSIGNEE = 4;

    private static final int REQUEST_CODE_CLOSE = 5;

    private static final int REQUEST_CODE_REOPEN = 6;

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

    private RepositoryId repositoryId;

    @InjectExtra(EXTRA_REPOSITORY_NAME)
    private String repository;

    @InjectExtra(EXTRA_REPOSITORY_OWNER)
    private String repositoryOwner;

    @InjectExtra(value = EXTRA_ISSUE_NUMBER)
    private int issueNumber;

    private Issue issue;

    @InjectExtra(value = ARG_COMMENTS, optional = true)
    private List<Comment> comments;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    @InjectView(android.R.id.list)
    private ListView list;

    private IssueHeaderViewHolder header;

    private View loadingView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_view);
        setTitle(getString(string.issue_title) + issueNumber);

        repositoryId = RepositoryId.create(repositoryOwner, repository);

        issue = store.getIssue(repositoryId, issueNumber);

        labelsDialog = new LabelsDialog(this, REQUEST_CODE_LABELS, repositoryId, labelService);
        milestoneDialog = new MilestoneDialog(this, REQUEST_CODE_MILESTONE, repositoryId, milestoneService);
        assigneeDialog = new AssigneeDialog(this, REQUEST_CODE_ASSIGNEE, repositoryId, collaboratorService);

        View headerView = getLayoutInflater().inflate(layout.issue_header, null);
        headerView.findViewById(id.ll_milestone).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    milestoneDialog.show(issue.getMilestone());
            }
        });
        headerView.findViewById(id.ll_assignee).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null) {
                    User assignee = issue.getAssignee();
                    assigneeDialog.show(assignee != null ? assignee.getLogin() : null);
                }
            }
        });
        headerView.findViewById(id.ll_state).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    confirmEditState(STATE_OPEN.equals(issue.getState()));
            }
        });
        header = new IssueHeaderViewHolder(headerView, avatarHelper, getResources());
        list.setFastScrollEnabled(true);
        list.addHeaderView(headerView);
        loadingView = getLayoutInflater().inflate(layout.issue_load_item, null);

        if (issue != null && comments != null)
            updateList(issue, comments);
        else {
            if (issue != null)
                header.updateViewFor(issue);
            refreshIssue();
        }
    }

    private void refreshIssue() {
        list.addHeaderView(loadingView);
        if (list.getAdapter() == null)
            list.setAdapter(new ArrayAdapter<Comment>(this, layout.comment_view_item));
        new RoboAsyncTask<FullIssue>(this) {

            public FullIssue call() throws Exception {
                Issue issue = store.refreshIssue(repositoryId, issueNumber);
                List<Comment> comments;
                if (issue.getComments() > 0)
                    comments = service.get(getContext()).getComments(repositoryId, issueNumber);
                else
                    comments = Collections.emptyList();
                return new FullIssue(issue, comments);
            }

            protected void onException(Exception e) throws RuntimeException {
                ErrorHelper.show(getContext(), e, string.error_issue_load);
            }

            protected void onSuccess(FullIssue fullIssue) throws Exception {
                issue = fullIssue.getIssue();
                comments = fullIssue.getComments();
                getIntent().putExtra(ARG_COMMENTS, (Serializable) fullIssue.getComments());
                updateList(fullIssue.getIssue(), fullIssue.getComments());
            }
        }.execute();
    }

    private void updateList(Issue issue, List<Comment> comments) {
        list.removeHeaderView(loadingView);
        header.updateViewFor(issue);
        list.setAdapter(new ViewHoldingListAdapter<Comment>(comments, ViewInflator.viewInflatorFor(this,
                layout.comment_view_item), ReflectiveHolderFactory.reflectiveFactoryFor(CommentViewHolder.class,
                avatarHelper)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_view, options);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (issue != null) {
            MenuItem stateItem = menu.findItem(id.issue_state);
            if (STATE_OPEN.equals(issue.getState()))
                stateItem.setTitle(string.close_issue);
            else
                stateItem.setTitle(string.reopen_issue);
        }
        return super.onPrepareOptionsMenu(menu);
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
            return true;
        case id.issue_state:
            if (issue != null)
                confirmEditState(STATE_OPEN.equals(issue.getState()));
            return true;
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
                refreshIssue();
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
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case REQUEST_CODE_LABELS:
            editLabels(arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED));
            break;
        case REQUEST_CODE_MILESTONE:
            editMilestone(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case REQUEST_CODE_ASSIGNEE:
            editAssignee(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case REQUEST_CODE_CLOSE:
            editState(true);
            break;
        case REQUEST_CODE_REOPEN:
            editState(false);
            break;
        }
    }

    private void confirmEditState(boolean close) {
        if (close)
            ConfirmDialogFragment.show(this, REQUEST_CODE_CLOSE, null, "Are you sure you want to close this issue?");
        else
            ConfirmDialogFragment.show(this, REQUEST_CODE_REOPEN, null, "Are you sure you want to reopen this issue?");
    }

    private void editState(final boolean close) {
        final ProgressDialog progress = new ProgressDialog(this);
        if (close)
            progress.setMessage("Closing issue...");
        else
            progress.setMessage("Reopening issue...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Issue>(this) {

            public Issue call() throws Exception {
                Issue editedIssue = new Issue();
                editedIssue.setNumber(issueNumber);
                if (close)
                    editedIssue.setState(STATE_CLOSED);
                else
                    editedIssue.setState(STATE_OPEN);
                return store.editIssue(repositoryId, editedIssue);
            }

            protected void onSuccess(Issue updated) throws Exception {
                header.updateViewFor(updated);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
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
                header.updateViewFor(updated);
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
                header.updateViewFor(updated);
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
                header.updateViewFor(updated);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(ViewIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }

            protected void onFinally() throws RuntimeException {
                progress.dismiss();
            };
        }.execute();
    }
}
