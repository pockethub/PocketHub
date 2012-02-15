package com.github.mobile.android.issue;

import android.app.ProgressDialog;

import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

import roboguice.util.RoboAsyncTask;

/**
 * Dialog helper to display a list of possibly selected issue labels
 */
public class LabelsDialog {

    private LabelService service;

    private List<Label> repositoryLabels;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final IRepositoryIdProvider repository;

    /**
     * Create dialog helper to display labels
     *
     * @param activity
     * @param requestCode
     * @param repository
     * @param service
     */
    public LabelsDialog(final DialogFragmentActivity activity, final int requestCode,
            final IRepositoryIdProvider repository, final LabelService service) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
        this.service = service;
    }

    private void load(final List<Label> selectedLabels) {
        final ProgressDialog loader = new ProgressDialog(activity);
        loader.setMessage("Loading Labels...");
        loader.show();
        new RoboAsyncTask<List<Label>>(activity) {

            public List<Label> call() throws Exception {
                repositoryLabels = service.getLabels(repository);
                Collections.sort(repositoryLabels, new Comparator<Label>() {

                    public int compare(Label l1, Label l2) {
                        return l1.getName().compareToIgnoreCase(l2.getName());
                    }
                });
                return repositoryLabels;
            }

            protected void onSuccess(List<Label> all) throws Exception {
                loader.dismiss();
                show(selectedLabels);
            }

            protected void onException(Exception e) throws RuntimeException {
                loader.dismiss();
            }
        }.execute();
    }

    /**
     * Show dialog with given labels selected
     *
     * @param selectedLabels
     */
    public void show(List<Label> selectedLabels) {
        if (repositoryLabels == null) {
            load(selectedLabels);
            return;
        }

        final String[] names = new String[repositoryLabels.size()];
        final boolean[] checked = new boolean[names.length];
        if (selectedLabels == null || selectedLabels.isEmpty())
            for (int i = 0; i < names.length; i++)
                names[i] = repositoryLabels.get(i).getName();
        else {
            Set<String> selectedNames = new HashSet<String>();
            for (Label label : selectedLabels)
                selectedNames.add(label.getName());
            for (int i = 0; i < names.length; i++) {
                names[i] = repositoryLabels.get(i).getName();
                if (selectedNames.contains(names[i]))
                    checked[i] = true;
            }
        }
        ConfirmDialogFragment.confirm(activity, requestCode, "Select Labels: ", null, names, checked);
    }
}
