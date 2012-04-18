package com.github.mobile.android.ui.issue;

import android.app.ProgressDialog;
import android.content.Context;

import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Task to comment on an issue in a repository
 */
public class CreateCommentTask extends ProgressDialogTask<Comment> {

    private final IRepositoryIdProvider repository;

    private final int issueNumber;

    @Inject
    private IssueService service;

    private String comment;

    /**
     * Create task for creating a comment on the given issue in the given repository
     *
     * @param context
     * @param repository
     * @param issueNumber
     */
    public CreateCommentTask(final Context context, final IRepositoryIdProvider repository, final int issueNumber) {
        super(context);

        this.repository = repository;
        this.issueNumber = issueNumber;
    }

    @Override
    protected Comment run() throws Exception {
        return service.createComment(repository, issueNumber, comment);
    }

    /**
     * Create comment
     *
     * @param comment
     * @return this task
     */
    public CreateCommentTask create(final String comment) {
        this.comment = comment;

        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setMessage(getString(string.creating_comment));
        progress.setIndeterminate(true);
        progress.show();

        execute();
        return this;
    }
}
