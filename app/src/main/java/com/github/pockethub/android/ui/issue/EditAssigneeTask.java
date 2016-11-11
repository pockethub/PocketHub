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

import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.BaseActivity;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Collections;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;

/**
 * Task to edit the assignee
 */
//TODO Let this take multiple assignees
public class EditAssigneeTask implements Observable.OnSubscribe<Issue> {

    @Inject
    private IssueStore store;

    private final AssigneeDialog assigneeDialog;

    private final BaseActivity activity;

    private final Repository repositoryId;

    private final int issueNumber;

    private final ProgressObserverAdapter<Issue> observer;

    private User assignee;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditAssigneeTask(final BaseActivity activity,
                            final Repository repositoryId, final int issueNumber,
                            final ProgressObserverAdapter<Issue> observer) {
        this.activity = activity;
        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        this.observer = observer;
        observer.setContent(R.string.updating_assignee);
        assigneeDialog = new AssigneeDialog(activity, ISSUE_ASSIGNEE_UPDATE,
                repositoryId);
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super Issue> subscriber) {
        try{
            String assigneLogin;
            if (assignee != null)
                assigneLogin = assignee.login();
            else
                assigneLogin = "";

            IssueRequest edit = IssueRequest.builder()
                    .assignees(Collections.singletonList(assigneLogin))
                    .build();
            subscriber.onNext(store.editIssue(repositoryId, issueNumber, edit));
        } catch (IOException e) {
            subscriber.onError(e);
        }
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
        this.assignee = user;

        Observable.create(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.<Issue>bindToLifecycle())
                .subscribe(observer);

        return this;
    }

}
