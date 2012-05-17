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
import android.text.TextUtils;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;

import java.util.ArrayList;
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

    private static final String TAG = "LabelsDialog";

    private final LabelService service;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final IRepositoryIdProvider repository;

    private Map<String, Label> labels;

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
        new ProgressDialogTask<List<Label>>(activity) {

            @Override
            public List<Label> run() throws Exception {
                List<Label> repositoryLabels = service.getLabels(repository);
                Map<String, Label> loadedLabels = new TreeMap<String, Label>(CASE_INSENSITIVE_ORDER);
                for (Label label : repositoryLabels)
                    loadedLabels.put(label.getName(), label);
                labels = loadedLabels;
                return repositoryLabels;
            }

            @Override
            protected void onSuccess(List<Label> all) throws Exception {
                super.onSuccess(all);

                show(selectedLabels);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Exception loading labels", e);
                ToastUtils.show(activity, e, string.error_labels_load);
            }

            @Override
            public void execute() {
                dismissProgress();
                showIndeterminate(string.loading_labels);

                super.execute();
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
        if (TextUtils.isEmpty(name))
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

        final ArrayList<Label> names = new ArrayList<Label>(labels.values());
        final boolean[] checked = new boolean[names.size()];
        if (selectedLabels != null && !selectedLabels.isEmpty()) {
            Set<String> selectedNames = new HashSet<String>();
            for (Label label : selectedLabels)
                selectedNames.add(label.getName());
            for (int i = 0; i < checked.length; i++)
                if (selectedNames.contains(names.get(i).getName()))
                    checked[i] = true;
        }
        LabelsDialogFragment
                .show(activity, requestCode, activity.getString(string.select_labels), null, names, checked);
    }
}
