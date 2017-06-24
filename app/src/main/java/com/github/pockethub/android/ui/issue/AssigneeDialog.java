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

import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.util.RxPageUtil;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueAssigneeService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog {

    private static final String TAG = "AssigneeDialog";

    private final int requestCode;

    private final BaseActivity activity;

    private final Single<List<User>> assigneeSingle;

    /**
     * Create dialog helper to display assignees
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public AssigneeDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        this.activity = activity;
        this.requestCode = requestCode;

        PageIterator.GitHubRequest<Response<Page<User>>> gitHubRequest = page -> ServiceGenerator
                .createService(activity, IssueAssigneeService.class)
                .getAssignees(repository.owner().login(), repository.name(), page);

        assigneeSingle = RxPageUtil.getAllPages(gitHubRequest, 1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toSortedList((o1, o2) -> CASE_INSENSITIVE_ORDER.compare(o1.login(), o2.login()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_collaborators))
                .cache();
    }

    /**
     * Show dialog with given assignee selected
     *
     * @param selectedAssignee
     */
    public void show(User selectedAssignee) {
        assigneeSingle.subscribe(collaborators -> {
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
        }, error -> {
            Log.d(TAG, "Exception loading collaborators", error);
            ToastUtils.show(activity, error, R.string.error_collaborators_load);
        });
    }
}
