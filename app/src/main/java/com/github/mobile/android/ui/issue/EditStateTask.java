package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.RequestCodes.ISSUE_CLOSE;
import static com.github.mobile.android.RequestCodes.ISSUE_REOPEN;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.app.ProgressDialog;

import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;

/**
 * Task to close or reopen an issue
 */
public class EditStateTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private boolean close;

    /**
     * Create task to edit issue state
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditStateTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
    }

    /**
     * Confirm action
     *
     * @param close
     * @return this task
     */
    public EditStateTask confirm(boolean close) {
        if (close)
            ConfirmDialogFragment.show((DialogFragmentActivity) getContext(), ISSUE_CLOSE, null,
                    "Are you sure you want to close this issue?");
        else
            ConfirmDialogFragment.show((DialogFragmentActivity) getContext(), ISSUE_REOPEN, null,
                    "Are you sure you want to reopen this issue?");

        return this;
    }

    protected Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        if (close)
            editedIssue.setState(STATE_CLOSED);
        else
            editedIssue.setState(STATE_OPEN);
        return store.editIssue(repositoryId, editedIssue);
    }

    /**
     * Edit state of issue
     *
     * @param close
     * @return this task
     */
    public EditStateTask edit(boolean close) {
        progress = new ProgressDialog(getContext());
        if (close)
            progress.setMessage("Closing issue...");
        else
            progress.setMessage("Reopening issue...");
        progress.setIndeterminate(true);
        progress.show();

        this.close = close;

        execute();
        return this;
    }
}
