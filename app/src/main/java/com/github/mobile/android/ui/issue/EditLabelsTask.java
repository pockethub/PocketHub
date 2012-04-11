package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.RequestCodes.ISSUE_LABELS_UPDATE;
import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.async.AuthenticatedUserTask;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.issue.LabelsDialog;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

/**
 * Task to edit labels
 */
public class EditLabelsTask extends AuthenticatedUserTask<Issue> {

    @Inject
    private IssueStore store;

    @Inject
    private LabelService service;

    private ProgressDialog progress;

    private final LabelsDialog labelsDialog;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private String[] labels;

    /**
     * Create task to edit labels
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditLabelsTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        labelsDialog = new LabelsDialog(activity, ISSUE_LABELS_UPDATE, repositoryId, service);
    }

    /**
     * Prompt for labels selection
     *
     * @param labels
     *            current labels
     * @return this task
     */
    public EditLabelsTask prompt(List<Label> labels) {
        labelsDialog.show(labels);
        return this;
    }

    /**
     * Edit issue to have given labels
     *
     * @param labels
     * @return this task
     */
    public EditLabelsTask edit(String[] labels) {
        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating labels...");
        progress.setIndeterminate(true);
        progress.show();

        this.labels = labels;

        execute();
        return this;
    }

    public Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        List<Label> issueLabels = new ArrayList<Label>(labels.length);
        for (String label : labels)
            issueLabels.add(new Label().setName(label));
        editedIssue.setLabels(issueLabels);
        return store.editIssue(repositoryId, editedIssue);
    }

    private void dismissProgress() {
        if (progress != null)
            progress.dismiss();
    }

    /**
     * Sub-classes may override but should always call super to ensure the progress dialog is dismissed
     */
    @Override
    protected void onSuccess(Issue issue) throws Exception {
        dismissProgress();
    }

    /**
     * Sub-classes may override but should always call super to ensure the progress dialog is dismissed
     */
    @Override
    protected void onException(Exception e) throws RuntimeException {
        dismissProgress();
    }
}
