package com.github.mobile.core.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;

/**
 * Issue model with comments
 */
public class FullIssue extends ArrayList<Comment> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 4586476132467323827L;

    private final Issue issue;

    /**
     * Create wrapper for issue and comments
     *
     * @param issue
     * @param comments
     */
    public FullIssue(final Issue issue, final Collection<Comment> comments) {
        super(comments);
        this.issue = issue;
    }

    /**
     * Create empty wrapper
     */
    public FullIssue() {
        this.issue = null;
    }

    /**
     * @return issue
     */
    public Issue getIssue() {
        return issue;
    }
}
