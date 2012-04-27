package com.github.mobile.ui.issue;

import static com.github.mobile.RequestCodes.ISSUE_MILESTONE_UPDATE;
import android.app.ProgressDialog;

import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.issue.IssueStore;
import com.github.mobile.issue.MilestoneDialog;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 * Task to edit a milestone
 */
public class EditMilestoneTask extends ProgressDialogTask<Issue> {

    @Inject
    private MilestoneService service;

    @Inject
    private IssueStore store;

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

        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating milestone...");
        progress.setIndeterminate(true);
        progress.show();

        super.execute();

        return this;
    }
}
