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
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseProgressDialog;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueMilestoneService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of milestones to select one from
 */
public class MilestoneDialog extends BaseProgressDialog {

    private static final String TAG = "MilestoneDialog";

    private ArrayList<Milestone> repositoryMilestones;

    private final int requestCode;

    private final BaseActivity activity;

    private final Repository repository;

    /**
     * Create dialog helper to display milestones
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public MilestoneDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        super(activity);
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    /**
     * Get milestones
     *
     * @return list of milestones
     */
    public List<Milestone> getMilestones() {
        return repositoryMilestones;
    }

    private void load(final Milestone selectedMilestone) {
        getPageAndNext(1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toSortedList((m1, m2) -> CASE_INSENSITIVE_ORDER.compare(m1.title(), m2.title()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_milestones))
                .subscribe(milestones -> {
                    repositoryMilestones = (ArrayList) milestones;

                    show(selectedMilestone);
                }, error -> {
                    Log.e(TAG, "Exception loading milestones", error);
                    ToastUtils.show(activity, error, R.string.error_milestones_load);
                });
    }

    private Observable<Page<Milestone>> getPageAndNext(int i) {
        return ServiceGenerator.createService(activity, IssueMilestoneService.class)
                .getRepositoryMilestones(repository.owner().login(), repository.name(), i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(response -> {
                    Page<Milestone> page = response.body();
                    if (page.next() == null) {
                        return Observable.just(page);
                    }

                    return Observable.just(page)
                            .concatWith(getPageAndNext(page.next()));
                });
    }

    /**
     * Show dialog with given milestone selected
     *
     * @param selectedMilestone
     */
    public void show(Milestone selectedMilestone) {
        if (repositoryMilestones == null) {
            load(selectedMilestone);
            return;
        }

        int checked = -1;
        if (selectedMilestone != null) {
            for (int i = 0; i < repositoryMilestones.size(); i++) {
                if (selectedMilestone.number() == repositoryMilestones.get(i).number()) {
                    checked = i;
                    break;
                }
            }
        }
        MilestoneDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_milestone), null,
                repositoryMilestones, checked);
    }

    /**
     * Get milestone number for title
     *
     * @param title
     * @return number of -1 if not found
     */
    public int getMilestoneNumber(String title) {
        if (repositoryMilestones == null) {
            return -1;
        }
        for (Milestone milestone : repositoryMilestones) {
            if (title.equals(milestone.title())) {
                return milestone.number();
            }
        }
        return -1;
    }
}
