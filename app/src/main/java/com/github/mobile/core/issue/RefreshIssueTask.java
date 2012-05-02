package com.github.mobile.core.issue;

import android.content.Context;
import android.util.Log;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.util.HtmlUtils;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Task to load and store and {@link Issue}
 */
public class RefreshIssueTask extends AuthenticatedUserTask<FullIssue> {

    private static final String TAG = "RefreshIssueTask";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    /**
     * Create task to refresh given issue
     *
     * @param context
     * @param repositoryId
     * @param issueNumber
     */
    public RefreshIssueTask(Context context, IRepositoryIdProvider repositoryId, int issueNumber) {
        super(context);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
    }

    @Override
    public FullIssue run() throws Exception {
        Issue issue = store.refreshIssue(repositoryId, issueNumber);
        List<Comment> comments;
        if (issue.getComments() > 0)
            comments = service.getComments(repositoryId, issueNumber);
        else
            comments = Collections.emptyList();
        for (Comment comment : comments)
            comment.setBodyHtml(HtmlUtils.format(comment.getBodyHtml()).toString());
        return new FullIssue(issue, comments);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading issue", e);
    }
}
