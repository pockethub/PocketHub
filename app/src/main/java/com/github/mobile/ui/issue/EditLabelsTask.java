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
import android.app.ProgressDialog;

import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.issue.IssueStore;
import com.github.mobile.issue.LabelsDialog;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

import java.util.ArrayList;
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

    private String[] labels;

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
    public EditLabelsTask edit(String[] labels) {
        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setMessage("Updating labels...");
        progress.setIndeterminate(true);
        progress.show();

        this.labels = labels;

        execute();
        return this;
    }

    public Issue run() throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        List<Label> issueLabels = new ArrayList<Label>(labels.length);
        for (String label : labels)
            issueLabels.add(new Label().setName(label));
        editedIssue.setLabels(issueLabels);
        return store.editIssue(repositoryId, editedIssue);
    }
}
