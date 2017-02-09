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

import com.github.pockethub.android.rx.ProgressObserverAdapter;
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
import java.util.Map;
import java.util.TreeMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog extends BaseProgressDialog {

    private static final String TAG = "AssigneeDialog";

    private Map<String, User> collaborators;

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
        getPageAndNext(1).subscribe(new ProgressObserverAdapter<Page<User>>(activity, R.string.loading_collaborators) {
            List<User> users = new ArrayList<>();

            @Override
            public void onError(Throwable error) {
                dismissProgress();
                Log.d(TAG, "Exception loading collaborators", error);
                ToastUtils.show(activity, error, R.string.error_collaborators_load);
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                Map<String, User> loadedCollaborators = new TreeMap<>(
                        CASE_INSENSITIVE_ORDER);
                for (User user : users)
                    loadedCollaborators.put(user.login(), user);
                collaborators = loadedCollaborators;

                dismissProgress();
                show(selectedAssignee);
            }

            @Override
            public void onNext(Page<User> page) {
                users.addAll(page.items());
            }
        }.start());
    }

    private Observable<Page<User>> getPageAndNext(int i) {
        return ServiceGenerator.createService(activity, IssueAssigneeService.class)
                .getAssignees(repository.owner().login(), repository.name(), i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Func1<Page<User>, Observable<Page<User>>>() {
                    @Override
                    public Observable<Page<User>> call(Page<User> page) {
                        if (page.next() == null)
                            return Observable.just(page);

                        return Observable.just(page)
                                .concatWith(getPageAndNext(page.next()));
                    }
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

        final ArrayList<User> users = new ArrayList<>(
                collaborators.values());
        int checked = -1;
        if (selectedAssignee != null)
            for (int i = 0; i < users.size(); i++)
                if (selectedAssignee.login().equals(users.get(i).login()))
                    checked = i;
        AssigneeDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_assignee), null, users,
                checked);
    }
}
