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
package com.github.pockethub.ui.issue;

import android.accounts.Account;

import com.alorma.github.sdk.bean.dto.request.EditIssueAssigneeRequestDTO;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.ProgressDialogTask;
import com.google.inject.Inject;

import static com.github.pockethub.RequestCodes.ISSUE_ASSIGNEE_UPDATE;

/**
 * Task to edit the assignee
 */
public class EditAssigneeTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final AssigneeDialog assigneeDialog;

    private final Repo repositoryId;

    private final int issueNumber;

    private User assignee;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditAssigneeTask(final DialogFragmentActivity activity,
            final Repo repositoryId, final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        assigneeDialog = new AssigneeDialog(activity, ISSUE_ASSIGNEE_UPDATE,
                repositoryId);
    }

    /**
     * Prompt for assignee selection
     *
     * @param assignee
     *            current assignee
     * @return this task
     */
    public EditAssigneeTask prompt(User assignee) {
        assigneeDialog.show(assignee);
        return this;
    }

    /**
     * Edit issue to have given assignee
     *
     * @param user
     * @return this task
     */
    public EditAssigneeTask edit(User user) {
        showIndeterminate(R.string.updating_assignee);

        this.assignee = user;

        execute();
        return this;
    }

    @Override
    protected Issue run(Account account) throws Exception {
        EditIssueAssigneeRequestDTO edit = new EditIssueAssigneeRequestDTO();
        if (assignee != null)
            edit.assignee = assignee.login;
        else
            edit.assignee = "";
        return store.editIssue(repositoryId, issueNumber, edit);
    }
}
