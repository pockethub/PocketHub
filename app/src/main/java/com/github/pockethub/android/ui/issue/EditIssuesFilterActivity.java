/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueFilter;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.InfoUtils;
import com.google.inject.Inject;
import java.util.List;

import static android.view.View.GONE;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE_FILTER;

/**
 * Activity to create or edit an issues filter for a repository
 */
public class EditIssuesFilterActivity extends BaseActivity {

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param filter
     * @return intent
     */
    public static Intent createIntent(IssueFilter filter) {
        return new Builder("repo.issues.filter.VIEW").add(EXTRA_ISSUE_FILTER,
            filter).toIntent();
    }

    private static final int REQUEST_LABELS = 1;

    private static final int REQUEST_MILESTONE = 2;

    private static final int REQUEST_ASSIGNEE = 3;

    @Inject
    private AvatarLoader avatars;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private IssueFilter filter;

    private TextView labelsText;

    private TextView milestoneText;

    private TextView assigneeText;

    private ImageView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_issues_filter_edit);

        labelsText = (TextView) findViewById(R.id.tv_labels);
        milestoneText = (TextView) findViewById(R.id.tv_milestone);
        assigneeText = (TextView) findViewById(R.id.tv_assignee);
        avatarView = (ImageView) findViewById(R.id.iv_avatar);

        if (savedInstanceState != null) {
            filter = savedInstanceState.getParcelable(EXTRA_ISSUE_FILTER);
        }

        if (filter == null) {
            filter = getIntent().getParcelableExtra(EXTRA_ISSUE_FILTER);
        }

        final Repository repository = filter.getRepository();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.filter_issues_title);
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, repository.owner());

        OnClickListener assigneeListener = v -> {
            if (assigneeDialog == null) {
                assigneeDialog = new AssigneeDialog(this, REQUEST_ASSIGNEE, repository);
            }
            assigneeDialog.show(filter.getAssignee());
        };

        findViewById(R.id.tv_assignee_label).setOnClickListener(assigneeListener);
        assigneeText.setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = v -> {
            if (milestoneDialog == null) {
                milestoneDialog = new MilestoneDialog(this, REQUEST_MILESTONE, repository);
            }
            milestoneDialog.show(filter.getMilestone());
        };

        findViewById(R.id.tv_milestone_label).setOnClickListener(milestoneListener);
        milestoneText.setOnClickListener(milestoneListener);

        OnClickListener labelsListener = v -> {
            if (labelsDialog == null) {
                labelsDialog = new LabelsDialog(this, REQUEST_LABELS, repository);
            }
            labelsDialog.show(filter.getLabels());
        };

        findViewById(R.id.tv_labels_label).setOnClickListener(labelsListener);
        labelsText.setOnClickListener(labelsListener);

        updateAssignee();
        updateMilestone();
        updateLabels();

        RadioGroup status = (RadioGroup) findViewById(R.id.issue_filter_status);
        RadioGroup sortOrder = (RadioGroup) findViewById(R.id.issue_sort_order);
        RadioGroup sortType = (RadioGroup) findViewById(R.id.issue_sort_type);

        status.setOnCheckedChangeListener((radioGroup, checkedId) ->
                filter.setOpen(checkedId == R.id.rb_open));

        if (filter.isOpen()) {
            status.check(R.id.rb_open);
        } else {
            status.check(R.id.rb_closed);
        }

        sortOrder.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.rb_asc) {
                filter.setDirection(IssueFilter.DIRECTION_ASCENDING);
            } else {
                filter.setDirection(IssueFilter.DIRECTION_DESCENDING);
            }
        });

        if (filter.getDirection().equals(IssueFilter.DIRECTION_ASCENDING)) {
            sortOrder.check(R.id.rb_asc);
        } else {
            sortOrder.check(R.id.rb_desc);
        }

        sortType.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_created:
                    filter.setSortType(IssueFilter.SORT_CREATED);
                    break;
                case R.id.rb_updated:
                    filter.setSortType(IssueFilter.SORT_UPDATED);
                    break;
                case R.id.rb_comments:
                    filter.setSortType(IssueFilter.SORT_COMMENTS);
                    break;
                default:
                    break;
            }
        });

        switch (filter.getSortType()) {
            case IssueFilter.SORT_CREATED:
                sortType.check(R.id.rb_created);
                break;
            case IssueFilter.SORT_UPDATED:
                sortType.check(R.id.rb_updated);
                break;
            case IssueFilter.SORT_COMMENTS:
                sortType.check(R.id.rb_comments);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(R.menu.activity_issue_filter, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_apply:
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

        outState.putParcelable(EXTRA_ISSUE_FILTER, filter);
    }

    private void updateLabels() {
        List<Label> selected = filter.getLabels();
        if (selected != null) {
            LabelDrawableSpan.setText(labelsText, selected);
        } else {
            labelsText.setText(R.string.none);
        }
    }

    private void updateMilestone() {
        Milestone selected = filter.getMilestone();
        if (selected != null) {
            milestoneText.setText(selected.title());
        } else {
            milestoneText.setText(R.string.none);
        }
    }

    private void updateAssignee() {
        User selected = filter.getAssignee();
        if (selected != null) {
            avatars.bind(avatarView, selected);
            assigneeText.setText(selected.login());
        } else {
            avatarView.setVisibility(GONE);
            assigneeText.setText(R.string.assignee_anyone);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

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
