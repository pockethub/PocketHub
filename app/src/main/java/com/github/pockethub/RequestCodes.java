/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub;

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

    /**
     * Request to view a commit
     */
    int COMMIT_VIEW = 10;

    /**
     * Request to update the current reference
     */
    int REF_UPDATE = 11;

    /**
     * Request to view a repository
     */
    int REPOSITORY_VIEW = 12;

    /**
     * Request to enter two-factor authentication OTP code
     */
    int OTP_CODE_ENTER = 13;

    /**
     * Request to edit a comment
     */
    int COMMENT_EDIT = 14;

    /**
     * Request to delete a comment
     */
    int COMMENT_DELETE = 15;
}
