package com.github.mobile.android.issue;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.util.GitHubIntents;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to create a persistent issues filter for a repository
 */
public class FilterIssuesActivity extends RoboActivity {

    @Inject
    private CollaboratorService collaborators;

    @Inject
    private MilestoneService milestones;

    @Inject
    private LabelService labels;

    private List<Label> allLabels;

    private List<User> allCollaborators;

    private List<Milestone> allMilestones;

    private Set<String> selectedLabels;

    private String selectedAssignee;

    private int selectedMilestone = -1;

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param repo
     * @return intent
     */
    public static Intent createIntent(Repository repo) {
        return new GitHubIntents.Builder("repo.issues.filter.VIEW").repo(repo).toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issues_filter);

        final Repository repository = (Repository) getIntent().getSerializableExtra(GitHubIntents.EXTRA_REPOSITORY);

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                if (allCollaborators == null) {
                    final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                    loader.setMessage("Loading Users..");
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

        ((Button) findViewById(id.b_apply)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                HashMap<String, String> filter = new HashMap<String, String>();

                Intent intent = new Intent();
                intent.putExtra(GitHubIntents.EXTRA_ISSUE_FILTER, filter);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void promptForLabels() {
        final Builder prompt = new Builder(FilterIssuesActivity.this);
        prompt.setTitle("Select Labels:");
        final String[] names = new String[allLabels.size()];
        final boolean[] checked = new boolean[names.length];
        if (selectedLabels == null)
            for (int i = 0; i < names.length; i++)
                names[i] = allLabels.get(i).getName();
        else
            for (int i = 0; i < names.length; i++) {
                names[i] = allLabels.get(i).getName();
                if (selectedLabels.contains(names[i]))
                    checked[i] = true;
            }

        final Set<String> selected = new TreeSet<String>();
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
                selectedLabels = selected;
                if (selected.size() == 1)
                    ((TextView) findViewById(id.tv_labels)).setText(selected.iterator().next());
                else if (!selected.isEmpty()) {
                    StringBuilder labelsLabel = new StringBuilder();
                    for (String label : selected)
                        labelsLabel.append(label).append(',').append(' ');
                    ((TextView) findViewById(id.tv_labels)).setText(labelsLabel.substring(0, labelsLabel.length() - 2));
                }
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                selectedLabels = null;
                ((TextView) findViewById(id.tv_labels)).setText("");
                dialog.dismiss();
            }
        });
        prompt.show();
    }

    private void promptForMilestone() {
        final Builder prompt = new Builder(FilterIssuesActivity.this);
        prompt.setTitle("Select A Milestone:");

        final String[] names = new String[allMilestones.size()];
        int selected = -1;
        for (int i = 0; i < names.length; i++) {
            names[i] = allMilestones.get(i).getTitle();
            if (selectedMilestone == allMilestones.get(i).getNumber())
                selected = i;
        }

        prompt.setSingleChoiceItems(names, selected, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                selectedMilestone = allMilestones.get(which).getNumber();
                ((TextView) findViewById(id.tv_milestone)).setText(names[which]);
                dialog.dismiss();
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                selectedMilestone = -1;
                ((TextView) findViewById(id.tv_milestone)).setText("");
                dialog.dismiss();
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
            if (logins[i].equals(selectedAssignee))
                selected = i;
        }

        prompt.setSingleChoiceItems(logins, selected, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                selectedAssignee = logins[which];
                ((TextView) findViewById(id.tv_assignee)).setText(selectedAssignee);
                dialog.dismiss();
            }
        });
        prompt.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                selectedAssignee = null;
                ((TextView) findViewById(id.tv_assignee)).setText("");
                dialog.dismiss();
            }
        });
        prompt.show();
    }
}
