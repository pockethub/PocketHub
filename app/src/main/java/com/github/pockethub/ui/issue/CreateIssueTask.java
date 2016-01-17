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
import android.app.Activity;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.request.IssueRequest;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.issues.PostNewIssueClient;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

/**
 * Task to create an {@link Issue}
 */
public class CreateIssueTask extends ProgressDialogTask<Issue> {

    private static final String TAG = "CreateIssueTask";

    @Inject
    private IssueStore store;

    private final Repo repository;

    private final IssueRequest request;

    /**
     * Create task to create an {@link Issue}
     *
     * @param activity
     * @param repository
    request     */
    public CreateIssueTask(final Activity activity, final Repo repository, final IssueRequest request) {
        super(activity);

        this.repository = repository;
        this.request = request;
    }

    /**
     * Create issue
     *
     * @return this task
     */
    public CreateIssueTask create() {
        showIndeterminate(R.string.creating_issue);

        execute();
        return this;
    }

    @Override
    public Issue run(Account account) throws Exception {
        return new PostNewIssueClient(InfoUtils.createRepoInfo(repository), request).observable().toBlocking().first();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.e(TAG, "Exception creating issue", e);
        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
