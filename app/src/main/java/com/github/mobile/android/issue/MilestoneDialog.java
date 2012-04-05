package com.github.mobile.android.issue;

import static com.google.common.collect.Lists.newArrayList;
import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.string;
import com.github.mobile.android.SingleChoiceDialogFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import com.github.mobile.android.async.AuthenticatedUserTask;

/**
 * Dialog helper to display a list of milestones to select one from
 */
public class MilestoneDialog {

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
                repositoryMilestones = newArrayList();
                repositoryMilestones.addAll(service.getMilestones(repository, IssueService.STATE_OPEN));
                repositoryMilestones.addAll(service.getMilestones(repository, IssueService.STATE_CLOSED));
                Collections.sort(repositoryMilestones, new Comparator<Milestone>() {

                    public int compare(Milestone m1, Milestone m2) {
                        return m1.getTitle().compareToIgnoreCase(m2.getTitle());
                    }
                });
                return repositoryMilestones;
            }

            protected void onSuccess(List<Milestone> all) throws Exception {
                if (!loader.isShowing())
                    return;

                loader.dismiss();
                show(selectedMilestone);
            }

            protected void onException(Exception e) throws RuntimeException {
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
