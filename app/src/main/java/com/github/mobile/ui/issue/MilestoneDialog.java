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

import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.app.ProgressDialog;
import android.util.Log;

import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.SingleChoiceDialogFragment;
import com.github.mobile.R.string;
import com.github.mobile.accounts.AuthenticatedUserTask;

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

    private List<Milestone> repositoryMilestones;

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
    public MilestoneDialog(final DialogFragmentActivity activity, final int requestCode,
            final IRepositoryIdProvider repository, final MilestoneService service) {
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
        final ProgressDialog loader = new ProgressDialog(activity);
        loader.setMessage("Loading Milestones...");
        loader.show();
        new AuthenticatedUserTask<List<Milestone>>(activity) {

            public List<Milestone> run() throws Exception {
                List<Milestone> milestones = new ArrayList<Milestone>();
                milestones.addAll(service.getMilestones(repository, STATE_OPEN));
                milestones.addAll(service.getMilestones(repository, STATE_CLOSED));
                Collections.sort(milestones, new Comparator<Milestone>() {

                    public int compare(Milestone m1, Milestone m2) {
                        return m1.getTitle().compareToIgnoreCase(m2.getTitle());
                    }
                });
                return milestones;
            }

            protected void onSuccess(List<Milestone> all) throws Exception {
                repositoryMilestones = all;
                if (!loader.isShowing())
                    return;

                loader.dismiss();
                show(selectedMilestone);
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception loading milestones", e);
                loader.dismiss();
            }

            protected void onInterrupted(Exception e) {
                loader.dismiss();
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

        final String[] names = new String[repositoryMilestones.size()];
        int checked = -1;
        if (selectedMilestone == null)
            for (int i = 0; i < names.length; i++)
                names[i] = repositoryMilestones.get(i).getTitle();
        else
            for (int i = 0; i < names.length; i++) {
                names[i] = repositoryMilestones.get(i).getTitle();
                if (selectedMilestone.getNumber() == repositoryMilestones.get(i).getNumber())
                    checked = i;
            }
        SingleChoiceDialogFragment.show(activity, requestCode, activity.getString(string.select_milestone), null,
                names, checked);
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
