package com.github.mobile.android.issue;

import static android.widget.Toast.LENGTH_LONG;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.string;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

import roboguice.util.RoboAsyncTask;

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
        new RoboAsyncTask<List<Label>>(activity) {

            public List<Label> call() throws Exception {
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
                Toast.makeText(activity, e.getMessage(), LENGTH_LONG).show();
                loader.dismiss();
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
