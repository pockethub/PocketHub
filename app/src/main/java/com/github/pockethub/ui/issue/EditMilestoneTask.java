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

import com.alorma.github.sdk.bean.dto.request.EditIssueMilestoneRequestDTO;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Milestone;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.service.MilestoneService;

import static com.github.pockethub.RequestCodes.ISSUE_MILESTONE_UPDATE;

/**
 * Task to edit a milestone
 */
public class EditMilestoneTask extends ProgressDialogTask<Issue> {

    @Inject
    private MilestoneService service;

    @Inject
    private IssueStore store;

    private final MilestoneDialog milestoneDialog;

    private final Repo repositoryId;

    private final int issueNumber;

    private int milestoneNumber;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditMilestoneTask(final DialogFragmentActivity activity,
            final Repo repositoryId, final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        milestoneDialog = new MilestoneDialog(activity, ISSUE_MILESTONE_UPDATE,
                repositoryId);
    }

    @Override
    protected Issue run(Account account) throws Exception {
        EditIssueMilestoneRequestDTO editedIssue = new EditIssueMilestoneRequestDTO();
        editedIssue.milestone = milestoneNumber;
        return store.editIssue(repositoryId, issueNumber, editedIssue);
    }

    /**
     * Prompt for milestone selection
     *
     * @param milestone
     *            current milestone
     * @return this task
     */
    public EditMilestoneTask prompt(Milestone milestone) {
        milestoneDialog.show(milestone);
        return this;
    }

    /**
     * Edit issue to have given milestone
     *
     * @param milestone
     * @return this task
     */
    public EditMilestoneTask edit(Milestone milestone) {
        if (milestone != null)
            milestoneNumber = milestone.number;
            milestoneNumber = -1;

        showIndeterminate(R.string.updating_milestone);

        super.execute();

        return this;
    }
}
