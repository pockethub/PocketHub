/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.Intents.EXTRA_ISSUE;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.mobile.Intents.EXTRA_USER;
import static com.github.mobile.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_MILESTONE_UPDATE;
import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.core.issue.IssueUtils;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.StyledText;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 * Activity to edit or create an issue
 */
public class EditIssueActivity extends DialogFragmentActivity {

    /**
     * Create intent to create an issue
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return createIntent(null, repository.getOwner().getLogin(),
                repository.getName(), repository.getOwner());
    }

    /**
     * Create intent to edit an issue
     *
     * @param issue
     * @param repositoryOwner
     * @param repositoryName
     * @param user
     * @return intent
     */
    public static Intent createIntent(final Issue issue,
            final String repositoryOwner, final String repositoryName,
            final User user) {
        Builder builder = new Builder("repo.issues.edit.VIEW");
        if (user != null)
            builder.add(EXTRA_USER, user);
        builder.add(EXTRA_REPOSITORY_NAME, repositoryName);
        builder.add(EXTRA_REPOSITORY_OWNER, repositoryOwner);
        if (issue != null)
            builder.issue(issue);
        return builder.toIntent();
    }

    private EditText titleText;

    private EditText bodyText;

    private View milestoneGraph;

    private TextView milestoneText;

    private View milestoneClosed;

    private ImageView assigneeAvatar;

    private TextView assigneeText;

    private TextView labelsText;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private MilestoneService milestoneService;

    @Inject
    private CollaboratorService collaboratorService;

    @Inject
    private LabelService labelService;

    private Issue issue;

    private RepositoryId repository;

    private MenuItem saveItem;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private LabelsDialog labelsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.issue_edit);

        titleText = finder.find(id.et_issue_title);
        bodyText = finder.find(id.et_issue_body);
        milestoneGraph = finder.find(id.ll_milestone_graph);
        milestoneText = finder.find(id.tv_milestone);
        milestoneClosed = finder.find(id.v_closed);
        assigneeAvatar = finder.find(id.iv_assignee_avatar);
        assigneeText = finder.find(id.tv_assignee_name);
        labelsText = finder.find(id.tv_labels);

        checkCollaboratorStatus();

        Intent intent = getIntent();

        if (savedInstanceState != null)
            issue = (Issue) savedInstanceState.getSerializable(EXTRA_ISSUE);
        if (issue == null)
            issue = (Issue) intent.getSerializableExtra(EXTRA_ISSUE);
        if (issue == null)
            issue = new Issue();

        repository = RepositoryId.create(
                intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
                intent.getStringExtra(EXTRA_REPOSITORY_NAME));

        ActionBar actionBar = getSupportActionBar();
        if (issue.getNumber() > 0)
            if (IssueUtils.isPullRequest(issue))
                actionBar.setTitle(getString(string.pull_request_title)
                        + issue.getNumber());
            else
                actionBar.setTitle(getString(string.issue_title)
                        + issue.getNumber());
        else
            actionBar.setTitle(string.new_issue);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, (User) intent.getSerializableExtra(EXTRA_USER));

        titleText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                updateSaveMenu(s);
            }
        });

        updateSaveMenu();
        titleText.setText(issue.getTitle());
        bodyText.setText(issue.getBody());
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            issue.setMilestone(MilestoneDialogFragment.getSelected(arguments));
            updateMilestone();
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            User assignee = AssigneeDialogFragment.getSelected(arguments);
            if (assignee != null)
                issue.setAssignee(assignee);
            else
                issue.setAssignee(new User().setLogin(""));
            updateAssignee();
            break;
        case ISSUE_LABELS_UPDATE:
            issue.setLabels(LabelsDialogFragment.getSelected(arguments));
            updateLabels();
            break;
        }
    }

    private void showMainContent() {
        finder.find(id.sv_issue_content).setVisibility(View.VISIBLE);
        finder.find(id.pb_loading).setVisibility(View.GONE);
    }

    private void showCollaboratorOptions() {
        finder.find(id.tv_milestone_label).setVisibility(View.VISIBLE);
        finder.find(id.ll_milestone).setVisibility(View.VISIBLE);
        finder.find(id.tv_labels_label).setVisibility(View.VISIBLE);
        finder.find(id.ll_labels).setVisibility(View.VISIBLE);
        finder.find(id.tv_assignee_label).setVisibility(View.VISIBLE);
        finder.find(id.ll_assignee).setVisibility(View.VISIBLE);

        finder.onClick(id.ll_milestone, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (milestoneDialog == null)
                    milestoneDialog = new MilestoneDialog(
                        EditIssueActivity.this, ISSUE_MILESTONE_UPDATE,
                        repository, milestoneService);
                milestoneDialog.show(issue.getMilestone());
            }
        });

        finder.onClick(id.ll_assignee, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (assigneeDialog == null)
                    assigneeDialog = new AssigneeDialog(EditIssueActivity.this,
                        ISSUE_ASSIGNEE_UPDATE, repository,
                        collaboratorService);
                assigneeDialog.show(issue.getAssignee());
            }
        });

        finder.onClick(id.ll_labels, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (labelsDialog == null)
                    labelsDialog = new LabelsDialog(EditIssueActivity.this,
                        ISSUE_LABELS_UPDATE, repository, labelService);
                labelsDialog.show(issue.getLabels());
            }
        });

        updateAssignee();
        updateLabels();
        updateMilestone();
    }

    private void updateMilestone() {
        Milestone milestone = issue.getMilestone();
        if (milestone != null) {
            milestoneText.setText(milestone.getTitle());
            float closed = milestone.getClosedIssues();
            float total = closed + milestone.getOpenIssues();
            if (total > 0) {
                ((LayoutParams) milestoneClosed.getLayoutParams()).weight = closed
                        / total;
                milestoneClosed.setVisibility(VISIBLE);
            } else
                milestoneClosed.setVisibility(GONE);
            milestoneGraph.setVisibility(VISIBLE);
        } else {
            milestoneText.setText(string.none);
            milestoneGraph.setVisibility(GONE);
        }
    }

    private void updateAssignee() {
        User assignee = issue.getAssignee();
        String login = assignee != null ? assignee.getLogin() : null;
        if (!TextUtils.isEmpty(login)) {
            assigneeText.setText(new StyledText().bold(login));
            assigneeAvatar.setVisibility(VISIBLE);
            avatars.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText(string.unassigned);
        }
    }

    private void updateLabels() {
        List<Label> labels = issue.getLabels();
        if (labels != null && !labels.isEmpty())
            LabelDrawableSpan.setText(labelsText, labels);
        else
            labelsText.setText(string.none);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_ISSUE, issue);
    }

    private void updateSaveMenu() {
        if (titleText != null)
            updateSaveMenu(titleText.getText());
    }

    private void updateSaveMenu(final CharSequence text) {
        if (saveItem != null)
            saveItem.setEnabled(!TextUtils.isEmpty(text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_edit, options);
        saveItem = options.findItem(id.m_apply);
        updateSaveMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_apply:
            issue.setTitle(titleText.getText().toString());
            issue.setBody(bodyText.getText().toString());
            if (issue.getNumber() > 0)
                new EditIssueTask(this, repository, issue) {

                    @Override
                    protected void onSuccess(Issue editedIssue)
                            throws Exception {
                        super.onSuccess(editedIssue);

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_ISSUE, editedIssue);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }.edit();
            else
                new CreateIssueTask(this, repository, issue) {

                    @Override
                    protected void onSuccess(Issue created) throws Exception {
                        super.onSuccess(created);

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_ISSUE, created);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                }.create();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void checkCollaboratorStatus() {
        new AuthenticatedUserTask<Boolean>(this) {

            @Override
            public Boolean run(Account account) throws Exception {
                return collaboratorService.isCollaborator(
                    repository, AccountUtils.getLogin(EditIssueActivity.this));
            }

            @Override
            protected void onSuccess(Boolean isCollaborator) throws Exception {
                super.onSuccess(isCollaborator);

                showMainContent();
                if(isCollaborator)
                    showCollaboratorOptions();
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                showMainContent();
            }
        }.execute();
    }
}
