package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.RequestCodes.ISSUE_MILESTONE_UPDATE;
import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.async.AuthenticatedUserTask;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.issue.MilestoneDialog;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 * Task to edit a milestone
 */
public class EditMilestoneTask extends AuthenticatedUserTask<Issue> {

    @Inject
    private MilestoneService service;

    @Inject
    private IssueStore store;

    private ProgressDialog progress;

    private final MilestoneDialog milestoneDialog;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private int milestoneNumber;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditMilestoneTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        milestoneDialog = new MilestoneDialog(activity, ISSUE_MILESTONE_UPDATE, repositoryId, service);
    }

    @Override
    protected Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        editedIssue.setMilestone(new Milestone().setNumber(milestoneNumber));
        return store.editIssue(repositoryId, editedIssue);
    }

    /**
     * Prompt for milestone selection
     *
     * @param milestone
     *            current milestone
     * @return this task
     */
    public EditMilestoneTask prompt(Milestone milestone) {
        milestoneDialog.show(milestone);
        return this;
    }

    /**
     * Edit issue to have given milestone
     *
     * @param title
     * @return this task
     */
    public EditMilestoneTask edit(String title) {
        if (title != null)
            milestoneNumber = milestoneDialog.getMilestoneNumber(title);
        else
            milestoneNumber = -1;

        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating milestone...");
        progress.setIndeterminate(true);
        progress.show();

        super.execute();

        return this;
    }

    private void dismissProgress() {
        if (progress != null)
            progress.dismiss();
    }

    /**
     * Sub-classes may override but should always call super to ensure the progress dialog is dismissed
     */
    @Override
    protected void onSuccess(Issue t) throws Exception {
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
