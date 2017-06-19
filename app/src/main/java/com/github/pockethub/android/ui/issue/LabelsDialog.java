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

import com.github.pockethub.android.rx.RxProgress;
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
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog helper to display a list of possibly selected issue labels
 */
public class LabelsDialog extends BaseProgressDialog {

    private static final String TAG = "LabelsDialog";

    private final int requestCode;

    private final BaseActivity activity;

    private final Repository repository;

    private List<Label> labels;

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
        getPageAndNext(1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toSortedList((o1, o2) -> CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_labels))
                .subscribe(loadedLabels -> {
                    labels = loadedLabels;

                    show(selectedLabels);
                }, error -> {
                    Log.e(TAG, "Exception loading labels", error);
                    ToastUtils.show(activity, error, R.string.error_labels_load);
                });
    }

    private Observable<Page<Label>> getPageAndNext(int i) {
        return ServiceGenerator.createService(activity, IssueLabelService.class)
                .getRepositoryLabels(repository.owner().login(), repository.name(), i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(response -> {
                    Page<Label> page = response.body();
                    if (page.next() == null) {
                        return Observable.just(page);
                    }

                    return Observable.just(page)
                            .concatWith(getPageAndNext(page.next()));
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

        final boolean[] checked = new boolean[labels.size()];
        if (selectedLabels != null && !selectedLabels.isEmpty()) {
            Set<String> selectedNames = new HashSet<>();
            for (Label label : selectedLabels) {
                selectedNames.add(label.name());
            }
            for (int i = 0; i < checked.length; i++) {
                if (selectedNames.contains(labels.get(i).name())) {
                    checked[i] = true;
                }
            }
        }
        LabelsDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_labels), null, new ArrayList<>(labels), checked);
    }
}
