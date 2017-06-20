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
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.BaseActivity;
import com.google.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import roboguice.RoboGuice;

import static com.github.pockethub.android.RequestCodes.ISSUE_CLOSE;
import static com.github.pockethub.android.RequestCodes.ISSUE_REOPEN;

/**
 * Task to close or reopen an issue
 */
public class EditStateTask {

    @Inject
    private IssueStore store;

    private final BaseActivity activity;
    private final Repository repository;

    private final int issueNumber;
    private final Consumer<Issue> observer;

    /**
     * Create task to edit issue state
     *
     * @param activity
     * @param repository
     * @param issueNumber
     */
    public EditStateTask(final BaseActivity activity,
                         final Repository repository, final int issueNumber,
                         final Consumer<Issue> observer) {
        this.activity = activity;
        this.repository = repository;
        this.issueNumber = issueNumber;
        this.observer = observer;
        RoboGuice.injectMembers(activity, this);
    }

    /**
     * Confirm action
     *
     * @param close
     * @return this task
     */
    public EditStateTask confirm(boolean close) {
        if (close) {
            ConfirmDialogFragment.show(activity, ISSUE_CLOSE, activity.getString(R.string.issue_confirm_close_title),
                    activity.getString(R.string.issue_confirm_close_message));
        } else {
            ConfirmDialogFragment.show(activity, ISSUE_REOPEN, activity.getString(R.string.issue_confirm_reopen_title),
                    activity.getString(R.string.issue_confirm_reopen_message));
        }

        return this;
    }

    /**
     * Edit state of issue.
     *
     * @param close
     * @return this task
     */
    public EditStateTask edit(boolean close) {
        int message = close ? R.string.closing_issue : R.string.reopening_issue;
        IssueState state = close ? IssueState.closed : IssueState.open;

        try {
            store.changeState(repository, issueNumber, state)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.bindToLifecycle())
                    .compose(RxProgress.bindToLifecycle(activity, message))
                    .subscribe(observer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
