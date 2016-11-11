/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui.issue;

import android.util.Log;

import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseProgressDialog;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueLabelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of possibly selected issue labels
 */
public class LabelsDialog extends BaseProgressDialog {

    private static final String TAG = "LabelsDialog";

    private final int requestCode;

    private final BaseActivity activity;

    private final Repository repository;

    private Map<String, Label> labels;

    /**
     * Create dialog helper to display labels
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public LabelsDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        super(activity);
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    private void load(final Collection<Label> selectedLabels) {
        getPageAndNext(1).subscribe(new ProgressObserverAdapter<Page<Label>>(activity, R.string.loading_labels){
            List<Label> repositoryLabels = new ArrayList<>();

            @Override
            public void onNext(Page<Label> page) {
                repositoryLabels.addAll(page.items());
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                Map<String, Label> loadedLabels = new TreeMap<>(
                        CASE_INSENSITIVE_ORDER);
                for (Label label : repositoryLabels)
                    loadedLabels.put(label.name(), label);

                labels = loadedLabels;

                dismissProgress();
                show(selectedLabels);
            }

            @Override
            public void onError(Throwable error) {
                dismissProgress();
                Log.e(TAG, "Exception loading labels", error);
                ToastUtils.show(activity, error, R.string.error_labels_load);
            }
        }.start());
    }

    private Observable<Page<Label>> getPageAndNext(int i) {
        return ServiceGenerator.createService(activity, IssueLabelService.class)
                .getRepositoryLabels(repository.owner().login(), repository.name(), i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Func1<Page<Label>, Observable<Page<Label>>>() {
                    @Override
                    public Observable<Page<Label>> call(Page<Label> page) {
                        if (page.next() == null)
                            return Observable.just(page);

                        return Observable.just(page)
                                .concatWith(getPageAndNext(page.next()));
                    }
                });
    }


    /**
     * Show dialog with given labels selected
     *
     * @param selectedLabels
     */
    public void show(Collection<Label> selectedLabels) {
        if (labels == null) {
            load(selectedLabels);
            return;
        }

        final ArrayList<Label> names = new ArrayList<>(labels.values());
        final boolean[] checked = new boolean[names.size()];
        if (selectedLabels != null && !selectedLabels.isEmpty()) {
            Set<String> selectedNames = new HashSet<>();
            for (Label label : selectedLabels)
                selectedNames.add(label.name());
            for (int i = 0; i < checked.length; i++)
                if (selectedNames.contains(names.get(i).name()))
                    checked[i] = true;
        }
        LabelsDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_labels), null, names, checked);
    }
}
