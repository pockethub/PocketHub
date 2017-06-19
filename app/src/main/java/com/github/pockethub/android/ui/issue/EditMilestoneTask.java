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

import com.github.pockethub.android.rx.RxProgress;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.BaseActivity;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;
import com.google.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import roboguice.RoboGuice;

import static com.github.pockethub.android.RequestCodes.ISSUE_MILESTONE_UPDATE;

/**
 * Task to edit a milestone
 */
public class EditMilestoneTask {

    @Inject
    private IssueStore store;

    private final MilestoneDialog milestoneDialog;

    private final BaseActivity activity;

    private final Repository repositoryId;

    private final int issueNumber;

    private int milestoneNumber;

    private final Consumer<Issue> observer;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditMilestoneTask(final BaseActivity activity,
                             final Repository repositoryId, final int issueNumber,
                             final Consumer<Issue> observer) {
        this.activity = activity;
        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        this.observer = observer;
        milestoneDialog = new MilestoneDialog(activity, ISSUE_MILESTONE_UPDATE,
                repositoryId);
        RoboGuice.injectMembers(activity, this);
    }

    /**
     * Prompt for milestone selection
     *
     * @param milestone current milestone
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
        if (milestone != null) {
            IssueRequest editedIssue = IssueRequest.builder().milestone(milestoneNumber).build();

            store.editIssue(repositoryId, issueNumber, editedIssue)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.bindToLifecycle())
                    .compose(RxProgress.bindToLifecycle(activity, R.string.updating_milestone))
                    .subscribe(observer);
        }

        return this;
    }
}
