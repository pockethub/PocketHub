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
import static com.github.mobile.Intents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.Set;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

import roboguice.inject.InjectView;

/**
 * Activity to create a persistent issues filter for a repository
 */
public class FilterIssuesActivity extends DialogFragmentActivity {

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param repo
     * @param filter
     * @return intent
     */
    public static Intent createIntent(Repository repo, IssueFilter filter) {
        return new Builder("repo.issues.filter.VIEW").repo(repo)
                .add(EXTRA_ISSUE_FILTER, filter).toIntent();
    }

    private static final int REQUEST_LABELS = 1;

    private static final int REQUEST_MILESTONE = 2;

    private static final int REQUEST_ASSIGNEE = 3;

    @Inject
    private CollaboratorService collaborators;

    @Inject
    private MilestoneService milestones;

    @Inject
    private LabelService labels;

    @Inject
    private AvatarLoader avatars;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private IssueFilter filter;

    @InjectView(id.tv_labels)
    private TextView labelsText;

    @InjectView(id.tv_milestone)
    private TextView milestoneText;

    @InjectView(id.tv_assignee)
    private TextView assigneeText;

    @InjectView(id.iv_avatar)
    private ImageView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.issues_filter);

        final Repository repository = (Repository) getIntent()
                .getSerializableExtra(EXTRA_REPOSITORY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.filter_issues_title);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());

        if (savedInstanceState != null)
            filter = (IssueFilter) savedInstanceState
                    .getSerializable(EXTRA_ISSUE_FILTER);

        if (filter == null)
            filter = (IssueFilter) getIntent().getSerializableExtra(
                    EXTRA_ISSUE_FILTER);

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                if (assigneeDialog == null)
                    assigneeDialog = new AssigneeDialog(
                            FilterIssuesActivity.this, REQUEST_ASSIGNEE,
                            repository, collaborators);
                assigneeDialog.show(filter.getAssignee());
            }
        };

        ((TextView) findViewById(id.tv_assignee_label))
                .setOnClickListener(assigneeListener);
        assigneeText.setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = new OnClickListener() {

            public void onClick(View v) {
                if (milestoneDialog == null)
                    milestoneDialog = new MilestoneDialog(
                            FilterIssuesActivity.this, REQUEST_MILESTONE,
                            repository, milestones);
                milestoneDialog.show(filter.getMilestone());
            }
        };

        ((TextView) findViewById(id.tv_milestone_label))
                .setOnClickListener(milestoneListener);
        milestoneText.setOnClickListener(milestoneListener);

        OnClickListener labelsListener = new OnClickListener() {

            public void onClick(View v) {
                if (labelsDialog == null)
                    labelsDialog = new LabelsDialog(FilterIssuesActivity.this,
                            REQUEST_LABELS, repository, labels);
                labelsDialog.show(filter.getLabels());
            }
        };

        ((TextView) findViewById(id.tv_labels_label))
                .setOnClickListener(labelsListener);
        labelsText.setOnClickListener(labelsListener);

        updateAssignee();
        updateMilestone();
        updateLabels();

        RadioButton openButton = (RadioButton) findViewById(id.rb_open);

        openButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked)
                    filter.setOpen(true);
            }
        });

        RadioButton closedButton = (RadioButton) findViewById(id.rb_closed);

        closedButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked)
                    filter.setOpen(false);
            }
        });

        if (filter.isOpen())
            openButton.setChecked(true);
        else
            closedButton.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_filter, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_apply:
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ISSUE_FILTER, filter);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_ISSUE_FILTER, filter);
    }

    private void updateLabels() {
        Set<Label> selected = filter.getLabels();
        if (selected != null)
            LabelDrawableSpan.setText(labelsText, selected);
        else
            labelsText.setText(string.none);
    }

    private void updateMilestone() {
        Milestone selected = filter.getMilestone();
        if (selected != null)
            milestoneText.setText(selected.getTitle());
        else
            milestoneText.setText(string.none);
    }

    private void updateAssignee() {
        User selected = filter.getAssignee();
        if (selected != null) {
            avatars.bind(avatarView, selected);
            assigneeText.setText(selected.getLogin());
        } else {
            avatarView.setVisibility(GONE);
            assigneeText.setText(string.assignee_anyone);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case REQUEST_LABELS:
            filter.setLabels(LabelsDialogFragment.getSelected(arguments));
            updateLabels();
            break;
        case REQUEST_MILESTONE:
            filter.setMilestone(MilestoneDialogFragment.getSelected(arguments));
            updateMilestone();
            break;
        case REQUEST_ASSIGNEE:
            filter.setAssignee(AssigneeDialogFragment.getSelected(arguments));
            updateAssignee();
            break;
        }
    }
}
