package com.github.mobile.android.issue;

import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.SingleChoiceDialogFragment;
import com.github.mobile.android.TextWatcherAdapter;
import com.github.mobile.android.async.AuthenticatedUserTask;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to create a new issue
 */
public class CreateIssueActivity extends DialogFragmentActivity {

    /**
     * Create intent to create an issue in the given {@link Repository}
     *
     * @param repo
     * @return intent
     */
    public static Intent createIntent(Repository repo) {
        return new Builder("repo.issues.create.VIEW").repo(repo).toIntent();
    }

    private static final String TAG = "CIA";

    private static final int REQUEST_CODE_LABELS = 1;

    private static final int REQUEST_CODE_MILESTONE = 2;

    private static final int REQUEST_CODE_ASSIGNEE = 3;

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @Inject
    private LabelService labelService;

    @Inject
    private MilestoneService milestoneService;

    @Inject
    private CollaboratorService collaboratorService;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private CreateIssueHeaderViewHolder header;

    @InjectView(id.et_issue_title)
    private EditText titleText;

    @InjectView(id.et_issue_body)
    private EditText bodyText;

    private final Issue newIssue = new Issue();

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_create, options);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_create);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.new_issue);
        actionBar.setSubtitle(repo.generateId());
        avatarHelper.bind(actionBar, repo.getOwner());

        labelsDialog = new LabelsDialog(this, REQUEST_CODE_LABELS, repo, labelService);
        milestoneDialog = new MilestoneDialog(this, REQUEST_CODE_MILESTONE, repo, milestoneService);
        assigneeDialog = new AssigneeDialog(this, REQUEST_CODE_ASSIGNEE, repo, collaboratorService);

        View headerView = findViewById(id.ll_issue_header);
        headerView.findViewById(id.ll_milestone).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                milestoneDialog.show(newIssue.getMilestone());
            }
        });
        headerView.findViewById(id.ll_assignee).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                User assignee = newIssue.getAssignee();
                assigneeDialog.show(assignee != null ? assignee.getLogin() : null);
            }
        });
        headerView.findViewById(id.ll_labels).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                labelsDialog.show(newIssue.getLabels());
            }
        });

        titleText.addTextChangedListener(new TextWatcherAdapter() {

            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

        header = new CreateIssueHeaderViewHolder(headerView, avatarHelper, getResources());
        header.updateViewFor(newIssue);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(id.issue_create).setEnabled(titleText.getText().toString().length() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case REQUEST_CODE_LABELS:
            String[] labelNames = arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED);
            if (labelNames != null && labelNames.length > 0) {
                List<Label> labels = new ArrayList<Label>(labelNames.length);
                for (String name : labelNames)
                    labels.add(labelsDialog.getLabel(name));
                newIssue.setLabels(labels);
            } else
                newIssue.setLabels(null);
            header.updateViewFor(newIssue);
            break;
        case REQUEST_CODE_MILESTONE:
            String title = arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED);
            if (title != null)
                newIssue.setMilestone(new Milestone().setTitle(title).setNumber(
                        milestoneDialog.getMilestoneNumber(title)));
            else
                newIssue.setMilestone(null);
            header.updateViewFor(newIssue);
            break;
        case REQUEST_CODE_ASSIGNEE:
            String login = arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED);

            if (login != null) {
                User assignee = assigneeDialog.getCollaborator(login);
                if (assignee == null)
                    assignee = new User().setLogin(login);
                newIssue.setAssignee(assignee);
            } else
                newIssue.setAssignee(null);
            header.updateViewFor(newIssue);
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.issue_create:
            createIssue();
            return true;
        case id.issue_labels:
            labelsDialog.show(newIssue.getLabels());
            return true;
        case id.issue_milestone:
            milestoneDialog.show(newIssue.getMilestone());
            return true;
        case id.issue_assignee:
            User assignee = newIssue.getAssignee();
            assigneeDialog.show(assignee != null ? assignee.getLogin() : null);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void createIssue() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Creating Issue...");
        progress.show();
        newIssue.setTitle(titleText.getText().toString());
        newIssue.setBody(bodyText.getText().toString());
        new AuthenticatedUserTask<Issue>(this) {

            public Issue run() throws Exception {
                return store.addIssue(service.createIssue(repo, newIssue));
            }

            protected void onSuccess(Issue issue) throws Exception {
                progress.cancel();
                startActivity(ViewIssueActivity.createIntent(issue));
                setResult(RESULT_OK);
                finish();
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.cancel();
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(CreateIssueActivity.this, e.getMessage(), LENGTH_LONG).show();
            }
        }.execute();
    }
}
