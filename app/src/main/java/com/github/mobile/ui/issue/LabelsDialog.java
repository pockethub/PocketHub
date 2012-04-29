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

import android.app.ProgressDialog;

import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.MultiChoiceDialogFragment;
import com.github.mobile.R.string;
import com.github.mobile.async.AuthenticatedUserTask;
import com.github.mobile.util.ToastUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

/**
 * Dialog helper to display a list of possibly selected issue labels
 */
public class LabelsDialog {

    private LabelService service;

    private Map<String, Label> labels;

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
        new AuthenticatedUserTask<List<Label>>(activity) {

            public List<Label> run() throws Exception {
                List<Label> repositoryLabels = service.getLabels(repository);
                Map<String, Label> loadedLabels = new TreeMap<String, Label>(new Comparator<String>() {

                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                for (Label label : repositoryLabels)
                    loadedLabels.put(label.getName(), label);
                labels = loadedLabels;
                return repositoryLabels;
            }

            protected void onSuccess(List<Label> all) throws Exception {
                if (!loader.isShowing())
                    return;

                loader.dismiss();
                show(selectedLabels);
            }

            protected void onException(Exception e) throws RuntimeException {
                loader.dismiss();
                ToastUtils.show(activity, e.getMessage());
            }
        }.execute();
    }

    /**
     * Get label with name
     *
     * @param name
     * @return label or null if none with name
     */
    public Label getLabel(String name) {
        if (labels == null)
            return null;
        if (name == null || name.length() == 0)
            return null;
        return labels.get(name);
    }

    /**
     * Show dialog with given labels selected
     *
     * @param selectedLabels
     */
    public void show(List<Label> selectedLabels) {
        if (labels == null) {
            load(selectedLabels);
            return;
        }

        final String[] names = labels.keySet().toArray(new String[labels.size()]);
        final boolean[] checked = new boolean[names.length];
        if (selectedLabels != null && !selectedLabels.isEmpty()) {
            Set<String> selectedNames = new HashSet<String>();
            for (Label label : selectedLabels)
                selectedNames.add(label.getName());
            for (int i = 0; i < names.length; i++)
                if (selectedNames.contains(names[i]))
                    checked[i] = true;
        }
        MultiChoiceDialogFragment.show(activity, requestCode, activity.getString(string.select_labels), null, names,
                checked);
    }
}
