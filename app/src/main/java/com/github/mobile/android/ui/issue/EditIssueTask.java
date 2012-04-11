package com.github.mobile.android.ui.issue;

import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;

/**
 * Task to edit an issue's title and/or body
 */
public class EditIssueTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final int issueNumber;

    private final IRepositoryIdProvider repositoryId;

    private String title;

    private String body;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditIssueTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
    }

    protected Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setTitle(title);
        editedIssue.setBody(body);
        editedIssue.setNumber(issueNumber);
        return store.editIssue(repositoryId, editedIssue);
    }

    /**
     * Edit issue
     *
     * @param title
     * @param body
     * @return this task
     */
    public EditIssueTask edit(final String title, final String body) {
        this.body = body;
        this.title = title;

        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating issue...");
        progress.setIndeterminate(true);
        progress.show();

        execute();
        return this;
    }
}
