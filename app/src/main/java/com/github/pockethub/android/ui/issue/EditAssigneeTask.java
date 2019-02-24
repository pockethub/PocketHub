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
package com.github.pockethub.android.ui.issue;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.base.BaseActivity;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;

import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;

/**
 * Task to edit the assignee
 */
//TODO Let this take multiple assignees
@AutoFactory
public class EditAssigneeTask {

    private final IssueStore store;

    private final AssigneeDialog assigneeDialog;

    private final BaseActivity activity;

    private final Repository repositoryId;

    private final int issueNumber;

    private final Consumer<Issue> observer;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditAssigneeTask(@Provided IssueStore store, final BaseActivity activity,
                            final Repository repositoryId, final int issueNumber,
                            final Consumer<Issue> observer) {
        this.store = store;
        this.activity = activity;
        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        this.observer = observer;
        assigneeDialog = new AssigneeDialog(activity, ISSUE_ASSIGNEE_UPDATE, repositoryId);
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
     * Edit issue to have given assignee.
     *
     * @param assignee The user the assign
     * @return this task
     */
    public EditAssigneeTask edit(User assignee) {
        String assigneeLogin = assignee != null ? assignee.login() : "";

        IssueRequest edit = IssueRequest.builder()
                .assignees(Collections.singletonList(assigneeLogin))
                .build();

        store.editIssue(repositoryId, issueNumber, edit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxProgress.bindToLifecycle(activity, R.string.updating_assignee))
                .as(AutoDisposeUtils.bindToLifecycle(activity))
                .subscribe(observer);

        return this;
    }
}
