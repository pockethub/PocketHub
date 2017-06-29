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
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueMilestoneService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of milestones to select one from
 */
public class MilestoneDialog {

    private static final String TAG = "MilestoneDialog";

    private final int requestCode;

    private final BaseActivity activity;

    private final Single<List<Milestone>> milestoneSingle;

    /**
     * Create dialog helper to display milestones
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public MilestoneDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        this.activity = activity;
        this.requestCode = requestCode;

        PageIterator.GitHubRequest<Response<Page<Milestone>>> gitHubRequest = page -> ServiceGenerator
                .createService(activity, IssueMilestoneService.class)
                .getRepositoryMilestones(repository.owner().login(), repository.name(), page);

        milestoneSingle = RxPageUtil.getAllPages(gitHubRequest, 1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toSortedList((m1, m2) -> CASE_INSENSITIVE_ORDER.compare(m1.title(), m2.title()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_milestones))
                .cache();
    }

    /**
     * Show dialog with given milestone selected
     *
     * @param selectedMilestone
     */
    public void show(Milestone selectedMilestone) {
        milestoneSingle.subscribe(milestones -> {
            int checked = -1;
            if (selectedMilestone != null) {
                for (int i = 0; i < milestones.size(); i++) {
                    if (selectedMilestone.number() == milestones.get(i).number()) {
                        checked = i;
                        break;
                    }
                }
            }
            MilestoneDialogFragment.show(activity, requestCode,
                    activity.getString(R.string.select_milestone), null,
                    new ArrayList<>(milestones), checked);
        }, error -> {
            Log.e(TAG, "Exception loading milestones", error);
            ToastUtils.show(activity, error, R.string.error_milestones_load);
        });
    }
}
