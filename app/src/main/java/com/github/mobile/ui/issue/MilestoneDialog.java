/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.issue;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 * Dialog helper to display a list of milestones to select one from
 */
public class MilestoneDialog {

    private static final String TAG = "MilestoneDialog";

    private MilestoneService service;

    private ArrayList<Milestone> repositoryMilestones;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final IRepositoryIdProvider repository;

    /**
     * Create dialog helper to display milestones
     *
     * @param activity
     * @param requestCode
     * @param repository
     * @param service
     */
    public MilestoneDialog(final DialogFragmentActivity activity,
            final int requestCode, final IRepositoryIdProvider repository,
            final MilestoneService service) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
        this.service = service;
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
        new ProgressDialogTask<ArrayList<Milestone>>(activity) {

            @Override
            public ArrayList<Milestone> run() throws Exception {
                ArrayList<Milestone> milestones = new ArrayList<Milestone>();
                milestones
                        .addAll(service.getMilestones(repository, STATE_OPEN));
                milestones.addAll(service.getMilestones(repository,
                        STATE_CLOSED));
                Collections.sort(milestones, new Comparator<Milestone>() {

                    public int compare(Milestone m1, Milestone m2) {
                        return CASE_INSENSITIVE_ORDER.compare(m1.getTitle(),
                                m2.getTitle());
                    }
                });
                return milestones;
            }

            @Override
            protected void onSuccess(ArrayList<Milestone> all) throws Exception {
                super.onSuccess(all);

                repositoryMilestones = all;
                show(selectedMilestone);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Exception loading milestones", e);
                ToastUtils.show(activity, e, string.error_milestones_load);
            }

            @Override
            public void execute() {
                showIndeterminate(string.loading_milestones);

                super.execute();
            }
        }.execute();
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
                if (selectedMilestone.getNumber() == repositoryMilestones
                        .get(i).getNumber()) {
                    checked = i;
                    break;
                }
        MilestoneDialogFragment.show(activity, requestCode,
                activity.getString(string.select_milestone), null,
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
            if (title.equals(milestone.getTitle()))
                return milestone.getNumber();
        return -1;
    }
}
