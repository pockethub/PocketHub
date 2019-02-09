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

import androidx.annotation.NonNull;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.BaseActivity;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.ISSUE_LABELS_UPDATE;

/**
 * Task to edit labels
 */
@AutoFactory
public class EditLabelsTask {

    private final IssueStore store;

    private final LabelsDialog labelsDialog;

    private final BaseActivity activity;

    private final Repository repositoryId;

    private final int issueNumber;

    private final Consumer<Issue> observer;

    /**
     * Create task to edit labels
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditLabelsTask(@Provided IssueStore store, final BaseActivity activity,
                          final Repository repositoryId, final int issueNumber,
                          final Consumer<Issue> observer) {

        this.activity = activity;
        this.store = store;
        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        this.observer = observer;
        labelsDialog = new LabelsDialog(activity, ISSUE_LABELS_UPDATE, repositoryId);
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
     * Edit issue to have given labels.
     *
     * @param labels
     * @return this task
     */
    public EditLabelsTask edit(@NonNull List<Label> labels) {
        List<String> labelNames = new ArrayList<>();
        for (Label label : labels) {
            labelNames.add(label.name());
        }

        IssueRequest editIssue = IssueRequest.builder().labels(labelNames).build();

        store.editIssue(repositoryId, issueNumber, editIssue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxProgress.bindToLifecycle(activity, R.string.updating_labels))
                .as(AutoDisposeUtils.bindToLifecycle(activity))
                .subscribe(observer);
        return this;
    }
}
