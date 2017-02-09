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
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.BaseActivity;
import com.google.inject.Inject;

import java.io.IOException;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.ISSUE_CLOSE;
import static com.github.pockethub.android.RequestCodes.ISSUE_REOPEN;

/**
 * Task to close or reopen an issue
 */
public class EditStateTask implements Observable.OnSubscribe<Issue> {

    @Inject
    private IssueStore store;

    private final BaseActivity activity;
    private final Repository repositoryId;

    private final int issueNumber;
    private final ProgressObserverAdapter<Issue> observer;

    private boolean close;

    /**
     * Create task to edit issue state
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditStateTask(final BaseActivity activity,
                         final Repository repositoryId, final int issueNumber,
                         final ProgressObserverAdapter<Issue> observer) {
        this.activity = activity;
        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        this.observer = observer;
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super Issue> subscriber) {
        try {
            IssueState state = close ? IssueState.closed : IssueState.open;
            subscriber.onNext(store.changeState(repositoryId, issueNumber, state));
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(e);
        }
    }

    /**
     * Confirm action
     *
     * @param close
     * @return this task
     */
    public EditStateTask confirm(boolean close) {
        if (close)
            ConfirmDialogFragment.show(activity, ISSUE_CLOSE, activity.getString(R.string.issue_confirm_close_title),
                    activity.getString(R.string.issue_confirm_close_message));
        else
            ConfirmDialogFragment.show(activity, ISSUE_REOPEN, activity.getString(R.string.issue_confirm_reopen_title),
                    activity.getString(R.string.issue_confirm_reopen_message));

        return this;
    }

    /**
     * Edit state of issue
     *
     * @param close
     * @return this task
     */
    public EditStateTask edit(boolean close) {
        int message = close ? R.string.closing_issue : R.string.reopening_issue;
        this.close = close;
        observer.setContent(message);
        observer.start();

        Observable.create(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.<Issue>bindToLifecycle())
                .subscribe(observer);

        return this;
    }
}
