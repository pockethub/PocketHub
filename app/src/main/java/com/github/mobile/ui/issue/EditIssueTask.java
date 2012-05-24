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
 * Task to edit an entire issue
 */
public class EditIssueTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final IRepositoryIdProvider repositoryId;

    private final Issue issue;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issue
     */
    public EditIssueTask(final DialogFragmentActivity activity, final IRepositoryIdProvider repositoryId,
            final Issue issue) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issue = issue;
    }

    protected Issue run() throws Exception {
        return store.editIssue(repositoryId, issue);
    }

    /**
     * Edit issue
     *
     * @return this task
     */
    public EditIssueTask edit() {
        dismissProgress();
        showIndeterminate(string.updating_issue);

        execute();
        return this;
    }
}
