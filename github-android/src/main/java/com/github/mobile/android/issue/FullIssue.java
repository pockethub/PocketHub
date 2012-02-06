package com.github.mobile.android.issue;

import java.io.Serializable;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;

/**
 * Issue model with comments
 */
public class FullIssue implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 4586476132467323827L;

    private final Issue issue;

    private final List<Comment> comments;

    /**
     * Create wrapper for issue and comments
     *
     * @param issue
     * @param comments
     */
    public FullIssue(final Issue issue, final List<Comment> comments) {
        this.issue = issue;
        this.comments = comments;
    }

    /**
     * @return issue
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * @return comments
     */
    public List<Comment> getComments() {
        return comments;
    }
}
