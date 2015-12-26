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

import com.alorma.github.sdk.bean.dto.request.EditIssueLabelsRequestDTO;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Label;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.ProgressDialogTask;
import com.google.inject.Inject;

import java.util.List;

import static com.github.pockethub.RequestCodes.ISSUE_LABELS_UPDATE;

/**
 * Task to edit labels
 */
public class EditLabelsTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final LabelsDialog labelsDialog;

    private final Repo repositoryId;

    private final int issueNumber;

    private Label[] labels;

    /**
     * Create task to edit labels
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditLabelsTask(final DialogFragmentActivity activity,
            final Repo repositoryId, final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        labelsDialog = new LabelsDialog(activity, ISSUE_LABELS_UPDATE,
                repositoryId);
    }

    /**
     * Prompt for labels selection
     *
     * @param labels
     *            current labels
     * @return this task
     */
    public EditLabelsTask prompt(List<Label> labels) {
        labelsDialog.show(labels);
        return this;
    }

    /**
     * Edit issue to have given labels
     *
     * @param labels
     * @return this task
     */
    public EditLabelsTask edit(Label[] labels) {
        showIndeterminate(R.string.updating_labels);

        this.labels = labels;

        execute();
        return this;
    }

    @Override
    public Issue run(Account account) throws Exception {
        EditIssueLabelsRequestDTO requestDTO = new EditIssueLabelsRequestDTO();
        requestDTO.labels = new String[labels.length];
        for (int i = 0; i < labels.length; i++)
            requestDTO.labels[i] = labels[i].name;

        return store.editIssue(repositoryId, issueNumber, requestDTO);
    }
}
