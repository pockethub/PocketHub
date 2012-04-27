package com.github.mobile;

/**
 * Request codes
 */
public interface RequestCodes {

    /**
     * Request to view a Gist
     */
    int GIST_VIEW = 1;

    /**
     * Request to view an issue
     */
    int ISSUE_VIEW = 2;

    /**
     * Request to edit an issue filter
     */
    int ISSUE_FILTER_EDIT = 3;

    /**
     * Request to create a new issue
     */
    int ISSUE_CREATE = 4;

    /**
     * Request to update an issue's labels
     */
    int ISSUE_LABELS_UPDATE = 2;

    /**
     * Request to update an issue's milestone
     */
    int ISSUE_MILESTONE_UPDATE = 3;

    /**
     * Request to update an issue's assignee
     */
    int ISSUE_ASSIGNEE_UPDATE = 4;

    /**
     * Request to close an issue
     */
    int ISSUE_CLOSE = 5;

    /**
     * Request to reopen an issue
     */
    int ISSUE_REOPEN = 6;

    /**
     * Request to edit an issue
     */
    int ISSUE_EDIT = 7;

    /**
     * Request to create a comment
     */
    int COMMENT_CREATE = 8;

    /**
     * Request to create a gist
     */
    int GIST_CREATE = 9;
}
