package com.github.mobile.issue;

import static com.github.mobile.util.GitHubIntents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.util.GitHubIntents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.MultiChoiceDialogFragment;
import com.github.mobile.SingleChoiceDialogFragment;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.util.GitHubIntents.Builder;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 * Activity to create a persistent issues filter for a repository
 */
public class FilterIssuesActivity extends DialogFragmentActivity {

    private static final int REQUEST_LABELS = 1;

    private static final int REQUEST_MILESTONE = 2;

    private static final int REQUEST_ASSIGNEE = 3;

    @Inject
    private CollaboratorService collaborators;

    @Inject
    private MilestoneService milestones;

    @Inject
    private LabelService labels;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private IssueFilter filter;

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param repo
     * @param filter
     * @return intent
     */
    public static Intent createIntent(Repository repo, IssueFilter filter) {
        return new Builder("repo.issues.filter.VIEW").repo(repo).add(EXTRA_ISSUE_FILTER, filter).toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issues_filter);

        final Repository repository = (Repository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.filter_issues_title);
        actionBar.setSubtitle(repository.generateId());

        if (savedInstanceState != null)
            filter = (IssueFilter) savedInstanceState.getSerializable(EXTRA_ISSUE_FILTER);

        if (filter == null)
            filter = ((IssueFilter) getIntent().getSerializableExtra(EXTRA_ISSUE_FILTER)).clone();

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                if (assigneeDialog == null)
                    assigneeDialog = new AssigneeDialog(FilterIssuesActivity.this, REQUEST_ASSIGNEE, repository,
                            collaborators);
                assigneeDialog.show(filter.getAssignee());
            }
        };

        ((TextView) findViewById(id.tv_assignee_label)).setOnClickListener(assigneeListener);
        ((TextView) findViewById(id.tv_assignee)).setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = new OnClickListener() {

            public void onClick(View v) {
                if (milestoneDialog == null)
                    milestoneDialog = new MilestoneDialog(FilterIssuesActivity.this, REQUEST_MILESTONE, repository,
                            milestones);
                milestoneDialog.show(filter.getMilestone());
            }
        };

        ((TextView) findViewById(id.tv_milestone_label)).setOnClickListener(milestoneListener);
        ((TextView) findViewById(id.tv_milestone)).setOnClickListener(milestoneListener);

        OnClickListener labelsListener = new OnClickListener() {

            public void onClick(View v) {
                if (labelsDialog == null)
                    labelsDialog = new LabelsDialog(FilterIssuesActivity.this, REQUEST_LABELS, repository, labels);
                Set<String> labelNames = filter.getLabels();
                if (labelNames != null) {
                    List<Label> filterLabels = new ArrayList<Label>(labelNames.size());
                    for (String name : labelNames)
                        filterLabels.add(new Label().setName(name));
                    labelsDialog.show(filterLabels);
                } else
                    labelsDialog.show(null);
            }
        };

        ((TextView) findViewById(id.tv_labels_label)).setOnClickListener(labelsListener);
        ((TextView) findViewById(id.tv_labels)).setOnClickListener(labelsListener);

        updateAssignee();
        updateMilestone();
        updateLabels();

        RadioButton openButton = (RadioButton) findViewById(id.rb_open);

        openButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    filter.setOpen(true);
            }
        });

        RadioButton closedButton = (RadioButton) findViewById(id.rb_closed);

        closedButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        case id.apply_filter:
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ISSUE_FILTER, filter);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_ISSUE_FILTER, filter);
    }

    private void updateLabels() {
        Set<String> selected = filter.getLabels();
        if (selected == null)
            ((TextView) findViewById(id.tv_labels)).setText("");
        else if (selected.size() == 1)
            ((TextView) findViewById(id.tv_labels)).setText(selected.iterator().next());
        else if (!selected.isEmpty())
            ((TextView) findViewById(id.tv_labels)).setText(Joiner.on(", ").join(selected));
    }

    private void updateMilestone() {
        Milestone selected = filter.getMilestone();
        if (selected != null)
            ((TextView) findViewById(id.tv_milestone)).setText(selected.getTitle());
        else
            ((TextView) findViewById(id.tv_milestone)).setText("");
    }

    private void updateAssignee() {
        String selected = filter.getAssignee();
        if (selected != null)
            ((TextView) findViewById(id.tv_assignee)).setText(selected);
        else
            ((TextView) findViewById(id.tv_assignee)).setText("");
    }

    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case REQUEST_LABELS:
            String[] labels = arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED);
            if (labels.length > 0)
                filter.setLabels(new HashSet<String>(Arrays.asList(labels)));
            else
                filter.setLabels(null);
            updateLabels();
            break;
        case REQUEST_MILESTONE:
            String milestone = arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED);
            if (milestone != null) {
                for (Milestone candidate : milestoneDialog.getMilestones())
                    if (milestone.equals(candidate.getTitle())) {
                        filter.setMilestone(candidate);
                        break;
                    }
            } else
                filter.setMilestone(null);
            updateMilestone();
            break;
        case REQUEST_ASSIGNEE:
            String assignee = arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED);
            filter.setAssignee(assignee);
            updateAssignee();
            break;
        }
    }
}
