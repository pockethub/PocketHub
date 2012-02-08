package com.github.mobile.android.issue;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.GitHubIntents;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

import roboguice.activity.RoboFragmentActivity;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to create a persistent issues filter for a repository
 */
public class FilterIssuesActivity extends RoboFragmentActivity {

    @Inject
    private CollaboratorService collaborators;

    @Inject
    private MilestoneService milestones;

    @Inject
    private LabelService labels;

    private List<Label> allLabels;

    private List<User> allCollaborators;

    private List<Milestone> allMilestones;

    private IssueFilter filter;

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param repo
     * @param filter
     * @return intent
     */
    public static Intent createIntent(Repository repo, IssueFilter filter) {
        return new GitHubIntents.Builder("repo.issues.filter.VIEW").repo(repo)
                .add(GitHubIntents.EXTRA_ISSUE_FILTER, filter).toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issues_filter);
        setTitle(string.filter_issues_title);

        final Repository repository = (Repository) getIntent().getSerializableExtra(GitHubIntents.EXTRA_REPOSITORY);

        if (savedInstanceState != null)
            filter = (IssueFilter) savedInstanceState.getSerializable(GitHubIntents.EXTRA_ISSUE_FILTER);

        if (filter == null)
            filter = ((IssueFilter) getIntent().getSerializableExtra(GitHubIntents.EXTRA_ISSUE_FILTER)).clone();

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                if (allCollaborators == null) {
                    final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                    loader.setMessage("Loading Collaborators..");
                    loader.show();
                    new RoboAsyncTask<List<User>>(FilterIssuesActivity.this) {

                        public List<User> call() throws Exception {
                            allCollaborators = collaborators.getCollaborators(repository);
                            Collections.sort(allCollaborators, new Comparator<User>() {

                                public int compare(User u1, User u2) {
                                    return u1.getLogin().compareToIgnoreCase(u2.getLogin());
                                }
                            });
                            return allCollaborators;
                        }

                        protected void onSuccess(List<User> users) throws Exception {
                            loader.dismiss();
                            promptForAssignee();
                        }

                        protected void onException(Exception e) throws RuntimeException {
                            loader.dismiss();
                        }
                    }.execute();
                } else
                    promptForAssignee();
            }
        };

        ((TextView) findViewById(id.tv_assignee_label)).setOnClickListener(assigneeListener);
        ((TextView) findViewById(id.tv_assignee)).setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = new OnClickListener() {

            public void onClick(View v) {
                if (allMilestones == null) {
                    final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                    loader.setMessage("Loading Milestones...");
                    loader.show();
                    new RoboAsyncTask<List<Milestone>>(FilterIssuesActivity.this) {

                        public List<Milestone> call() throws Exception {
                            List<Milestone> all = new ArrayList<Milestone>();
                            all.addAll(milestones.getMilestones(repository, IssueService.STATE_OPEN));
                            all.addAll(milestones.getMilestones(repository, IssueService.STATE_CLOSED));
                            Collections.sort(all, new Comparator<Milestone>() {

                                public int compare(Milestone m1, Milestone m2) {
                                    return m1.getTitle().compareToIgnoreCase(m2.getTitle());
                                }
                            });
                            allMilestones = all;
                            return allMilestones;
                        }

                        protected void onSuccess(List<Milestone> all) throws Exception {
                            loader.dismiss();
                            promptForMilestone();
                        }

                        protected void onException(Exception e) throws RuntimeException {
                            loader.dismiss();
                        }
                    }.execute();
                } else
                    promptForMilestone();
            }
        };

        ((TextView) findViewById(id.tv_milestone_label)).setOnClickListener(milestoneListener);
        ((TextView) findViewById(id.tv_milestone)).setOnClickListener(milestoneListener);

        OnClickListener labelsListener = new OnClickListener() {

            public void onClick(View v) {
                if (allLabels == null) {
                    final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                    loader.setMessage("Loading Labels...");
                    loader.show();
                    new RoboAsyncTask<List<Label>>(FilterIssuesActivity.this) {

                        public List<Label> call() throws Exception {
                            allLabels = labels.getLabels(repository);
                            Collections.sort(allLabels, new Comparator<Label>() {

                                public int compare(Label l1, Label l2) {
                                    return l1.getName().compareToIgnoreCase(l2.getName());
                                }
                            });
                            return allLabels;
                        }

                        protected void onSuccess(List<Label> all) throws Exception {
                            loader.dismiss();
                            promptForLabels();
                        }

                        protected void onException(Exception e) throws RuntimeException {
                            loader.dismiss();
                        }
                    }.execute();
                } else
                    promptForLabels();
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
                    filter.setOpenOnly();
            }
        });

        RadioButton closedButton = (RadioButton) findViewById(id.rb_closed);

        closedButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    filter.setClosedOnly();
            }
        });

        RadioButton allButton = (RadioButton) findViewById(id.rb_all);

        allButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    filter.setAll();
            }
        });

        if (filter.isAll())
            allButton.setChecked(true);
        else if (filter.isClosedOnly())
            closedButton.setChecked(true);
        else if (filter.isOpenOnly())
            openButton.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(menu.issue_filter, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.apply_filter:
            Intent intent = new Intent();
            intent.putExtra(GitHubIntents.EXTRA_ISSUE_FILTER, filter);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GitHubIntents.EXTRA_ISSUE_FILTER, filter);
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

    private void promptForLabels() {
        final Builder prompt = new Builder(FilterIssuesActivity.this);
        prompt.setTitle("Select Labels:");
        final String[] names = new String[allLabels.size()];
        final boolean[] checked = new boolean[names.length];
        Set<String> selectedLabels = filter.getLabels();
        final Set<String> selected = new TreeSet<String>();
        if (selectedLabels == null)
            for (int i = 0; i < names.length; i++)
                names[i] = allLabels.get(i).getName();
        else {
            selected.addAll(selectedLabels);
            for (int i = 0; i < names.length; i++) {
                names[i] = allLabels.get(i).getName();
                if (selectedLabels.contains(names[i]))
                    checked[i] = true;
            }
        }

        prompt.setMultiChoiceItems(names, checked, new OnMultiChoiceClickListener() {

            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked)
                    selected.add(names[which]);
                else
                    selected.remove(names[which]);
            }
        });
        prompt.setPositiveButton("Apply", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setLabels(selected);
                updateLabels();
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setLabels(null);
                updateLabels();
            }
        });
        prompt.show();
    }

    private void promptForMilestone() {
        final Builder prompt = new Builder(FilterIssuesActivity.this);
        prompt.setTitle("Select A Milestone:");

        final String[] names = new String[allMilestones.size()];
        int selected = -1;
        Milestone milestone = filter.getMilestone();
        if (milestone != null)
            for (int i = 0; i < names.length; i++) {
                names[i] = allMilestones.get(i).getTitle();
                if (milestone.getNumber() == allMilestones.get(i).getNumber())
                    selected = i;
            }
        else
            for (int i = 0; i < names.length; i++)
                names[i] = allMilestones.get(i).getTitle();

        prompt.setSingleChoiceItems(names, selected, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setMilestone(allMilestones.get(which));
                updateMilestone();
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setMilestone(null);
                updateMilestone();
            }
        });
        prompt.show();
    }

    private void promptForAssignee() {
        final Builder prompt = new Builder(FilterIssuesActivity.this);
        prompt.setTitle("Select An Assignee:");

        final String[] logins = new String[allCollaborators.size()];
        int selected = -1;
        for (int i = 0; i < logins.length; i++) {
            logins[i] = allCollaborators.get(i).getLogin();
            if (logins[i].equals(filter.getAssignee()))
                selected = i;
        }

        prompt.setSingleChoiceItems(logins, selected, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setAssignee(logins[which]);
                updateAssignee();
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                filter.setAssignee(null);
                updateAssignee();
            }
        });
        prompt.show();
    }
}
