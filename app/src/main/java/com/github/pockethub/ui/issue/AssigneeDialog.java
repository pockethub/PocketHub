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

import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.issues.GetAssigneesClient;
import com.github.pockethub.R;
import com.github.pockethub.rx.ObserverAdapter;
import com.github.pockethub.ui.BaseProgressDialog;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog extends BaseProgressDialog {

    private static final String TAG = "AssigneeDialog";

    private Map<String, User> collaborators;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final Repo repository;

    /**
     * Create dialog helper to display assignees
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public AssigneeDialog(final DialogFragmentActivity activity,
            final int requestCode, final Repo repository) {
        super(activity);
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    private void load(final User selectedAssignee) {
        showIndeterminate(R.string.loading_collaborators);
        new GetAssigneesClient(InfoUtils.createRepoInfo(repository)).observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.<List<User>>bindToLifecycle())
                .subscribe(new ObserverAdapter<List<User>>() {
                    @Override
                    public void onError(Throwable error) {
                        dismissProgress();
                        Log.d(TAG, "Exception loading collaborators", error);
                        ToastUtils.show(activity, error, R.string.error_collaborators_load);
                    }

                    @Override
                    public void onNext(List<User> users) {
                        Map<String, User> loadedCollaborators = new TreeMap<>(
                                CASE_INSENSITIVE_ORDER);
                        for (User user : users)
                            loadedCollaborators.put(user.login, user);
                        collaborators = loadedCollaborators;

                        dismissProgress();
                        show(selectedAssignee);
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
                if (selectedAssignee.login.equals(users.get(i).login))
                    checked = i;
        AssigneeDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_assignee), null, users,
                checked);
    }
}
