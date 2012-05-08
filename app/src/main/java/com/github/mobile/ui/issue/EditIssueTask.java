/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.issue;

import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
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
        showIndeterminate(string.updating_issue);

        execute();
        return this;
    }
}
