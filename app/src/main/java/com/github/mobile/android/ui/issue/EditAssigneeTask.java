package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.issue.AssigneeDialog;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;

/**
 * Task to edit the assignee
 */
public class EditAssigneeTask extends ProgressDialogTask<Issue> {

    @Inject
    private CollaboratorService service;

    @Inject
    private IssueStore store;

    private final AssigneeDialog assigneeDialog;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private String assignee;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditAssigneeTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        assigneeDialog = new AssigneeDialog(activity, ISSUE_ASSIGNEE_UPDATE, repositoryId, service);
    }

    /**
     * Prompt for assignee selection
     *
     * @param assignee
     *            current assignee
     * @return this task
     */
    public EditAssigneeTask prompt(String assignee) {
        assigneeDialog.show(assignee);
        return this;
    }

    /**
     * Edit issue to have given assignee
     *
     * @param user
     * @return this task
     */
    public EditAssigneeTask edit(String user) {
        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating assignee...");
        progress.setIndeterminate(true);
        progress.show();

        this.assignee = user;

        execute();
        return this;
    }

    protected Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setAssignee(new User().setLogin(assignee != null ? assignee : ""));
        editedIssue.setNumber(issueNumber);
        return store.editIssue(repositoryId, editedIssue);
    }
}
