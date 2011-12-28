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

import com.github.mobile.android.R;
import com.github.mobile.android.util.GitHubIntents;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
        setContentView(R.layout.issues_filter);

        final Repository repository = (Repository) getIntent().getSerializableExtra(GitHubIntents.EXTRA_REPOSITORY);

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                loader.setMessage("Loading Collaborators");
                loader.show();
                new RoboAsyncTask<List<User>>(FilterIssuesActivity.this) {

                    public List<User> call() throws Exception {
                        return collaborators.getCollaborators(repository);
                    }

                    protected void onSuccess(List<User> users) throws Exception {
                        loader.dismiss();
                        final Builder prompt = new Builder(FilterIssuesActivity.this);
                        prompt.setTitle("Select An Assignee:");
                        final String[] logins = new String[users.size()];
                        for (int i = 0; i < logins.length; i++)
                            logins[i] = users.get(i).getLogin();
                        Arrays.sort(logins, new Comparator<String>() {

                            public int compare(String s1, String s2) {
                                return s1.compareToIgnoreCase(s2);
                            }
                        });
                        prompt.setSingleChoiceItems(logins, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                ((TextView) findViewById(R.id.tv_assignee)).setText(logins[which]);
                                dialog.dismiss();
                            }
                        });
                        prompt.show();
                    }
                }.execute();
            }
        };

        ((TextView) findViewById(R.id.tv_assignee_label)).setOnClickListener(assigneeListener);
        ((TextView) findViewById(R.id.tv_assignee)).setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = new OnClickListener() {

            public void onClick(View v) {
                final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                loader.setMessage("Loading Milestones");
                loader.show();
                new RoboAsyncTask<List<Milestone>>(FilterIssuesActivity.this) {

                    public List<Milestone> call() throws Exception {
                        List<Milestone> all = new LinkedList<Milestone>();
                        all.addAll(milestones.getMilestones(repository, IssueService.STATE_OPEN));
                        all.addAll(milestones.getMilestones(repository, IssueService.STATE_CLOSED));
                        return all;
                    }

                    protected void onSuccess(List<Milestone> all) throws Exception {
                        loader.dismiss();
                        final Builder prompt = new Builder(FilterIssuesActivity.this);
                        prompt.setTitle("Select A Milestone:");
                        final String[] names = new String[all.size()];
                        for (int i = 0; i < names.length; i++)
                            names[i] = all.get(i).getTitle();

                        Arrays.sort(names, new Comparator<String>() {

                            public int compare(String s1, String s2) {
                                return s1.compareToIgnoreCase(s2);
                            }
                        });
                        prompt.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                ((TextView) findViewById(R.id.tv_milestone)).setText(names[which]);
                                dialog.dismiss();
                            }
                        });
                        prompt.show();
                    }
                }.execute();
            }
        };

        ((TextView) findViewById(R.id.tv_milestone_label)).setOnClickListener(milestoneListener);
        ((TextView) findViewById(R.id.tv_milestone)).setOnClickListener(milestoneListener);

        OnClickListener labelsListener = new OnClickListener() {

            public void onClick(View v) {
                final ProgressDialog loader = new ProgressDialog(FilterIssuesActivity.this);
                loader.setMessage("Loading Labels");
                loader.show();
                new RoboAsyncTask<List<Label>>(FilterIssuesActivity.this) {

                    public List<Label> call() throws Exception {
                        return labels.getLabels(repository);
                    }

                    protected void onSuccess(List<Label> all) throws Exception {
                        loader.dismiss();
                        final Builder prompt = new Builder(FilterIssuesActivity.this);
                        prompt.setTitle("Select Labels:");
                        final String[] names = new String[all.size()];
                        for (int i = 0; i < names.length; i++)
                            names[i] = all.get(i).getName();

                        Arrays.sort(names, new Comparator<String>() {

                            public int compare(String s1, String s2) {
                                return s1.compareToIgnoreCase(s2);
                            }
                        });

                        final Set<String> selected = new TreeSet<String>();
                        prompt.setMultiChoiceItems(names, null, new OnMultiChoiceClickListener() {

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
                                if (selected.size() == 1)
                                    ((TextView) findViewById(R.id.tv_labels)).setText(selected.iterator().next());
                                else if (!selected.isEmpty()) {
                                    StringBuilder selectedLabels = new StringBuilder();
                                    for (String label : selected)
                                        selectedLabels.append(label).append(',').append(' ');
                                    ((TextView) findViewById(R.id.tv_labels)).setText(selectedLabels.substring(0,
                                            selectedLabels.length() - 2));
                                }
                            }
                        });
                        prompt.show();
                    }
                }.execute();
            }
        };

        ((TextView) findViewById(R.id.tv_labels_label)).setOnClickListener(labelsListener);
        ((TextView) findViewById(R.id.tv_labels)).setOnClickListener(labelsListener);

        ((Button) findViewById(R.id.b_apply)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                HashMap<String, String> filter = new HashMap<String, String>();

                Intent intent = new Intent();
                intent.putExtra(GitHubIntents.EXTRA_ISSUE_FILTER, filter);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
