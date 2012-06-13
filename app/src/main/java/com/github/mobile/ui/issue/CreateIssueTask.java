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

import android.app.Activity;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Task to create an {@link Issue}
 */
public class CreateIssueTask extends ProgressDialogTask<Issue> {

    private static final String TAG = "CreateIssueTask";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private final IRepositoryIdProvider repository;

    private final Issue issue;

    /**
     * Create task to create an {@link Issue}
     *
     * @param activity
     * @param repository
     * @param issue
     */
    public CreateIssueTask(final Activity activity,
            final IRepositoryIdProvider repository, final Issue issue) {
        super(activity);

        this.repository = repository;
        this.issue = issue;
    }

    /**
     * Create issue
     *
     * @return this task
     */
    public CreateIssueTask create() {
        showIndeterminate(string.creating_issue);

        execute();
        return this;
    }

    @Override
    public Issue run() throws Exception {
        return store.addIssue(service.createIssue(repository, issue));
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.e(TAG, "Exception creating issue", e);
        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
