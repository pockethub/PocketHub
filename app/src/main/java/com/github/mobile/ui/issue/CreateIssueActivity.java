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
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.SingleChoiceDialogFragment;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
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

    private static final int REQUEST_CODE_LABELS = 1;

    private static final int REQUEST_CODE_MILESTONE = 2;

    private static final int REQUEST_CODE_ASSIGNEE = 3;

    @Inject
    private AvatarLoader avatarHelper;

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

    @InjectView(id.et_issue_title)
    private EditText titleText;

    @InjectView(id.et_issue_body)
    private EditText bodyText;

    @InjectView(id.tv_assignee_name)
    private TextView assigneeText;

    @InjectView(id.iv_assignee_avatar)
    private ImageView assigneeAvatar;

    @InjectView(id.tv_labels)
    private TextView labelsArea;

    @InjectView(id.tv_milestone)
    private TextView milestoneText;

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
                assigneeDialog.show(newIssue.getAssignee());
            }
        });
        headerView.findViewById(id.tv_labels).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                labelsDialog.show(newIssue.getLabels());
            }
        });

        titleText.addTextChangedListener(new TextWatcherAdapter() {

            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

        updateHeader(newIssue);
    }

    private void updateHeader(Issue issue) {
        User assignee = issue.getAssignee();
        if (assignee != null) {
            assigneeText.setText(assignee.getLogin());
            assigneeAvatar.setVisibility(VISIBLE);
            avatarHelper.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText("Unassigned");
        }

        List<Label> labels = issue.getLabels();
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(labelsArea, labels);
            labelsArea.setVisibility(VISIBLE);
        } else
            labelsArea.setVisibility(GONE);

        if (issue.getMilestone() != null)
            milestoneText.setText(issue.getMilestone().getTitle());
        else
            milestoneText.setText(milestoneText.getContext().getString(string.no_milestone));
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
            ArrayList<Label> labels = LabelsDialogFragment.getSelected(arguments);
            if (labels != null && !labels.isEmpty())
                newIssue.setLabels(labels);
            else
                newIssue.setLabels(null);
            updateHeader(newIssue);
            break;
        case REQUEST_CODE_MILESTONE:
            String title = arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED);
            if (title != null)
                newIssue.setMilestone(new Milestone().setTitle(title).setNumber(
                        milestoneDialog.getMilestoneNumber(title)));
            else
                newIssue.setMilestone(null);
            updateHeader(newIssue);
            break;
        case REQUEST_CODE_ASSIGNEE:
            newIssue.setAssignee(AssigneeDialogFragment.getSelected(arguments));
            updateHeader(newIssue);
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
            assigneeDialog.show(newIssue.getAssignee());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void createIssue() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(string.creating_issue));
        progress.show();
        newIssue.setTitle(titleText.getText().toString());
        newIssue.setBody(bodyText.getText().toString());
        new CreateIssueTask(this, repo, newIssue) {

            @Override
            protected void onSuccess(Issue issue) throws Exception {
                super.onSuccess(issue);

                startActivity(ViewIssuesActivity.createIntent(issue));
                setResult(RESULT_OK);
                finish();
            }

        }.create();
    }
}
