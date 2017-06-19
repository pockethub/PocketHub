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

import android.util.Log;

import com.github.pockethub.android.rx.RxProgress;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseProgressDialog;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueAssigneeService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog extends BaseProgressDialog {

    private static final String TAG = "AssigneeDialog";

    private List<User> collaborators;

    private final int requestCode;

    private final BaseActivity activity;

    private final Repository repository;

    /**
     * Create dialog helper to display assignees
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public AssigneeDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        super(activity);
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    private void load(final User selectedAssignee) {
        getPageAndNext(1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toSortedList((o1, o2) -> CASE_INSENSITIVE_ORDER.compare(o1.login(), o2.login()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_collaborators))
                .subscribe(loadedCollaborators -> {
                    collaborators = loadedCollaborators;

                    show(selectedAssignee);
                }, error -> {
                    Log.d(TAG, "Exception loading collaborators", error);
                    ToastUtils.show(activity, error, R.string.error_collaborators_load);
                });
    }

    private Observable<Page<User>> getPageAndNext(int i) {
        return ServiceGenerator.createService(activity, IssueAssigneeService.class)
                .getAssignees(repository.owner().login(), repository.name(), i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(response -> {
                    Page<User> page = response.body();
                    if (page.next() == null) {
                        return Observable.just(page);
                    }

                    return Observable.just(page)
                            .concatWith(getPageAndNext(page.next()));
                });
    }


    /**
     * Show dialog with given assignee selected
     *
     * @param selectedAssignee
     */
    public void show(User selectedAssignee) {
        if (collaborators == null) {
            load(selectedAssignee);
            return;
        }

        int checked = -1;
        if (selectedAssignee != null) {
            for (int i = 0; i < collaborators.size(); i++) {
                if (selectedAssignee.login().equals(collaborators.get(i).login())) {
                    checked = i;
                }
            }
        }
        AssigneeDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_assignee), null, new ArrayList<>(collaborators),
                checked);
    }
}
