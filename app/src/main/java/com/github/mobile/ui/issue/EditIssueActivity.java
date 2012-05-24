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
import static com.github.mobile.Intents.EXTRA_SUBTITLE;
import static com.github.mobile.Intents.EXTRA_TITLE;
import static com.github.mobile.Intents.EXTRA_USER;
import static com.github.mobile.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_MILESTONE_UPDATE;
import android.content.Intent;
import android.os.Bundle;
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
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.StyledText;
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

import roboguice.inject.InjectView;

/**
 * Activity to edit or create an issue
 */
public class EditIssueActivity extends DialogFragmentActivity {

    /**
     * Create intent to create an issue
     *
     * @param repository
     * @param title
     * @return intent
     */
    public static Intent createIntent(Repository repository, final String title) {
        return createIntent(null, repository.getOwner().getLogin(), repository.getName(), title,
                repository.generateId(), repository.getOwner());

    }

    /**
     * Create intent to edit an issue
     *
     * @param issue
     * @param repositoryOwner
     * @param repositoryName
     * @param title
     * @param subtitle
     * @param user
     * @return intent
     */
    public static Intent createIntent(final Issue issue, final String repositoryOwner, final String repositoryName,
            final String title, final String subtitle, final User user) {
        Builder builder = new Builder("repo.issues.edit.VIEW");
        if (title != null)
            builder.add(EXTRA_TITLE, title);
        if (subtitle != null)
            builder.add(EXTRA_SUBTITLE, subtitle);
        if (user != null)
            builder.add(EXTRA_USER, user);
        builder.add(EXTRA_REPOSITORY_NAME, repositoryName);
        builder.add(EXTRA_REPOSITORY_OWNER, repositoryOwner);
        if (issue != null)
            builder.issue(issue);
        return builder.toIntent();
    }

    @InjectView(id.et_issue_title)
    private EditText titleText;

    @InjectView(id.et_issue_body)
    private EditText bodyText;

    @InjectView(id.ll_milestone_graph)
    private View milestoneGraph;

    @InjectView(id.tv_milestone)
    private TextView milestoneText;

    @InjectView(id.v_closed)
    private View milestoneClosed;

    @InjectView(id.iv_assignee_avatar)
    private ImageView assigneeAvatar;

    @InjectView(id.tv_assignee_name)
    private TextView assigneeText;

    @InjectView(id.tv_labels)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.issue_edit);

        Intent intent = getIntent();

        if (savedInstanceState != null)
            issue = (Issue) savedInstanceState.getSerializable(EXTRA_ISSUE);
        if (issue == null)
            issue = (Issue) intent.getSerializableExtra(EXTRA_ISSUE);
        if (issue == null)
            issue = new Issue();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(intent.getStringExtra(EXTRA_TITLE));
        actionBar.setSubtitle(intent.getStringExtra(EXTRA_SUBTITLE));
        avatars.bind(actionBar, (User) intent.getSerializableExtra(EXTRA_USER));

        repository = RepositoryId.create(intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
                intent.getStringExtra(EXTRA_REPOSITORY_NAME));

        findViewById(id.ll_milestone).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new MilestoneDialog(EditIssueActivity.this, ISSUE_MILESTONE_UPDATE, repository, milestoneService)
                        .show(issue.getMilestone());
            }
        });

        findViewById(id.ll_assignee).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new AssigneeDialog(EditIssueActivity.this, ISSUE_ASSIGNEE_UPDATE, repository, collaboratorService)
                        .show(issue.getAssignee());
            }
        });

        findViewById(id.ll_labels).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new LabelsDialog(EditIssueActivity.this, ISSUE_LABELS_UPDATE, repository, labelService).show(issue
                        .getLabels());
            }
        });

        updateView();
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            issue.setMilestone(MilestoneDialogFragment.getSelected(arguments));
            updateView();
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            User assignee = AssigneeDialogFragment.getSelected(arguments);
            if (assignee != null)
                issue.setAssignee(assignee);
            else
                issue.setAssignee(new User().setLogin(""));
            updateView();
            break;
        case ISSUE_LABELS_UPDATE:
            issue.setLabels(LabelsDialogFragment.getSelected(arguments));
            updateView();
            break;
        }
    }

    private void updateView() {
        titleText.setText(issue.getTitle());
        bodyText.setText(issue.getBody());

        User assignee = issue.getAssignee();
        if (assignee != null) {
            StyledText name = new StyledText();
            name.bold(assignee.getLogin());
            assigneeText.setText(name);
            assigneeAvatar.setVisibility(VISIBLE);
            avatars.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText(string.unassigned);
        }

        List<Label> labels = issue.getLabels();
        if (labels != null && !labels.isEmpty())
            LabelDrawableSpan.setText(labelsText, labels);
        else
            labelsText.setText(string.none);

        if (issue.getMilestone() != null) {
            Milestone milestone = issue.getMilestone();
            milestoneText.setText(milestone.getTitle());
            float closed = milestone.getClosedIssues();
            float total = closed + milestone.getOpenIssues();
            if (total > 0) {
                ((LayoutParams) milestoneClosed.getLayoutParams()).weight = closed / total;
                milestoneClosed.setVisibility(VISIBLE);
            } else
                milestoneClosed.setVisibility(GONE);
            milestoneGraph.setVisibility(VISIBLE);
        } else {
            milestoneText.setText(string.none);
            milestoneGraph.setVisibility(GONE);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_ISSUE, issue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_edit, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.issue_edit:
            issue.setTitle(titleText.getText().toString());
            issue.setBody(bodyText.getText().toString());
            if (issue.getNumber() > 0)
                new EditIssueTask(this, repository, issue) {

                    @Override
                    protected void onSuccess(Issue editedIssue) throws Exception {
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
}
