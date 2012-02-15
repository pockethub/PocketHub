package com.github.mobile.android.issue;

import android.app.ProgressDialog;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.SingleChoiceDialogFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;

import roboguice.util.RoboAsyncTask;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog {

    private CollaboratorService service;

    private List<User> collaborators;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final IRepositoryIdProvider repository;

    /**
     * Create dialog helper to display assignees
     *
     * @param activity
     * @param requestCode
     * @param repository
     * @param service
     */
    public AssigneeDialog(final DialogFragmentActivity activity, final int requestCode,
            final IRepositoryIdProvider repository, final CollaboratorService service) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
        this.service = service;
    }

    private void load(final String selectedAssignee) {
        final ProgressDialog loader = new ProgressDialog(activity);
        loader.setMessage("Loading Collaborators...");
        loader.show();
        new RoboAsyncTask<List<User>>(activity) {

            public List<User> call() throws Exception {
                collaborators = service.getCollaborators(repository);
                Collections.sort(collaborators, new Comparator<User>() {

                    public int compare(User u1, User u2) {
                        return u1.getLogin().compareToIgnoreCase(u2.getLogin());
                    }
                });
                return collaborators;
            }

            protected void onSuccess(List<User> all) throws Exception {
                loader.dismiss();
                show(selectedAssignee);
            }

            protected void onException(Exception e) throws RuntimeException {
                loader.dismiss();
            }
        }.execute();
    }

    /**
     * Show dialog with given assignee selected
     *
     * @param selectedAssignee
     */
    public void show(String selectedAssignee) {
        if (collaborators == null) {
            load(selectedAssignee);
            return;
        }

        final String[] names = new String[collaborators.size()];
        int checked = -1;
        if (selectedAssignee == null)
            for (int i = 0; i < names.length; i++)
                names[i] = collaborators.get(i).getLogin();
        else
            for (int i = 0; i < names.length; i++) {
                names[i] = collaborators.get(i).getLogin();
                if (selectedAssignee.equals(names[i]))
                    checked = i;
            }
        SingleChoiceDialogFragment.show(activity, requestCode, "Select Assignee: ", null, names, checked);
    }
}
