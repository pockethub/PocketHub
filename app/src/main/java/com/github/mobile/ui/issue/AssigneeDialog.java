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

import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.ui.SingleChoiceDialogFragment;
import com.github.mobile.util.ToastUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;

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
        new ProgressDialogTask<List<User>>(activity) {

            @Override
            public List<User> run() throws Exception {
                List<User> users = service.getCollaborators(repository);
                Map<String, User> loadedCollaborators = new TreeMap<String, User>(CASE_INSENSITIVE_ORDER);
                for (User user : users)
                    loadedCollaborators.put(user.getLogin(), user);
                collaborators = loadedCollaborators;
                return users;
            }

            @Override
            protected void onSuccess(List<User> all) throws Exception {
                super.onSuccess(all);

                show(selectedAssignee);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ToastUtils.show(activity, e, string.error_collaborators_load);
            }

            @Override
            public void execute() {
                dismissProgress();
                showIndeterminate(string.loading_collaborators);

                super.execute();
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
        SingleChoiceDialogFragment.show(activity, requestCode, activity.getString(string.select_assignee), null, names,
                checked);
    }
}
