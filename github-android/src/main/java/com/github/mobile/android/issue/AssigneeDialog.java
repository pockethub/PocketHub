package com.github.mobile.android.issue;

import static android.widget.Toast.LENGTH_LONG;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.SingleChoiceDialogFragment;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;

import roboguice.util.RoboAsyncTask;

/**
 * Dialog helper to display a list of assignees to select one from
 */
public class AssigneeDialog {

    private CollaboratorService service;

    private Map<String, User> collaborators;

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
                List<User> users = service.getCollaborators(repository);
                Map<String, User> loadedCollaborators = new TreeMap<String, User>(new Comparator<String>() {

                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                for (User user : users)
                    loadedCollaborators.put(user.getLogin(), user);
                collaborators = loadedCollaborators;
                return users;
            }

            protected void onSuccess(List<User> all) throws Exception {
                loader.dismiss();
                show(selectedAssignee);
            }

            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(activity, e.getMessage(), LENGTH_LONG).show();
                loader.dismiss();
            }
        }.execute();
    }

    /**
     * Get collaborator with login
     *
     * @param login
     * @return collaborator or null if none found with login
     */
    public User getCollaborator(String login) {
        if (collaborators == null)
            return null;
        if (login == null || login.length() == 0)
            return null;
        return collaborators.get(login);
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

        final String[] names = collaborators.keySet().toArray(new String[collaborators.size()]);
        int checked = -1;
        if (selectedAssignee != null)
            for (int i = 0; i < names.length; i++)
                if (selectedAssignee.equals(names[i]))
                    checked = i;
        SingleChoiceDialogFragment.show(activity, requestCode, "Select Assignee: ", null, names, checked);
    }
}
