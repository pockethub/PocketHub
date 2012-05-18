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

import static com.github.mobile.RequestCodes.ISSUE_LABELS_UPDATE;

import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

/**
 * Task to edit labels
 */
public class EditLabelsTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    @Inject
    private LabelService service;

    private final LabelsDialog labelsDialog;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private Label[] labels;

    /**
     * Create task to edit labels
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditLabelsTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        labelsDialog = new LabelsDialog(activity, ISSUE_LABELS_UPDATE, repositoryId, service);
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
        dismissProgress();
        showIndeterminate(string.updating_labels);

        this.labels = labels;

        execute();
        return this;
    }

    @Override
    public Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        if (labels != null && labels.length > 0)
            editedIssue.setLabels(Arrays.asList(labels));
        return store.editIssue(repositoryId, editedIssue);
    }
}
