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

import com.alorma.github.sdk.bean.dto.response.Milestone;
import com.alorma.github.sdk.bean.dto.response.MilestoneState;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.issues.GetMilestonesClient;
import com.github.pockethub.R;
import com.github.pockethub.rx.ObserverAdapter;
import com.github.pockethub.ui.BaseProgressDialog;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of milestones to select one from
 */
public class MilestoneDialog extends BaseProgressDialog {

    private static final String TAG = "MilestoneDialog";

    private ArrayList<Milestone> repositoryMilestones;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final Repo repository;

    /**
     * Create dialog helper to display milestones
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public MilestoneDialog(final DialogFragmentActivity activity,
            final int requestCode, final Repo repository) {
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
        showIndeterminate(R.string.loading_milestones);
        new GetMilestonesClient(InfoUtils.createRepoInfo(repository), MilestoneState.open)
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.<List<Milestone>>bindToLifecycle())
                .subscribe(new ObserverAdapter<List<Milestone>>() {
                    @Override
                    public void onNext(List<Milestone> milestones) {
                        Collections.sort(milestones, new Comparator<Milestone>() {
                            public int compare(Milestone m1, Milestone m2) {
                                return CASE_INSENSITIVE_ORDER.compare(m1.title,
                                        m2.title);
                            }
                        });
                        repositoryMilestones = (ArrayList<Milestone>) milestones;

                        dismissProgress();
                        show(selectedMilestone);
                    }

                    @Override
                    public void onError(Throwable error) {
                        dismissProgress();
                        Log.e(TAG, "Exception loading milestones", error);
                        ToastUtils.show(activity, error, R.string.error_milestones_load);
                    }
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
        if (selectedMilestone != null)
            for (int i = 0; i < repositoryMilestones.size(); i++)
                if (selectedMilestone.number == repositoryMilestones.get(i).number) {
                    checked = i;
                    break;
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
        if (repositoryMilestones == null)
            return -1;
        for (Milestone milestone : repositoryMilestones)
            if (title.equals(milestone.title))
                return milestone.number;
        return -1;
    }
}
