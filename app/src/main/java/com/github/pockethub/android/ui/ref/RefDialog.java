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
package com.github.pockethub.android.ui.ref;

import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.GitReference;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.git.GetReferencesClient;
import com.alorma.gitskarios.core.Pair;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog to select a branch or tag
 */
public class RefDialog {

    private static final String TAG = "RefDialog";

    private Map<String, GitReference> refs;

    private final int requestCode;

    private final BaseActivity activity;

    private final Repo repository;

    /**
     * Create dialog helper to display refs
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public RefDialog(final BaseActivity activity,
            final int requestCode, final Repo repository) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    private void load(final GitReference selectedRef) {
        new GetReferencesClient(InfoUtils.createRepoInfo(repository))
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProgressObserverAdapter<Pair<List<GitReference>, Integer>>(activity, R.string.loading_refs) {
                    List<GitReference> allRefs = new ArrayList<>();

                    @Override
                    public void onNext(Pair<List<GitReference>, Integer> pair) {
                        super.onNext(pair);
                        allRefs.addAll(pair.first);
                    }

                    @Override
                    public void onCompleted() {
                        Map<String, GitReference> loadedRefs = new TreeMap<>(CASE_INSENSITIVE_ORDER);

                        for (GitReference ref : allRefs)
                            if (RefUtils.isValid(ref))
                                loadedRefs.put(ref.ref, ref);

                        refs = loadedRefs;
                        show(selectedRef);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.d(TAG, "Exception loading references", e);
                        ToastUtils.show(activity, e, R.string.error_refs_load);
                    }
                }.start());
    }

    /**
     * Show dialog with given reference selected
     *
     * @param selectedRef
     */
    public void show(GitReference selectedRef) {
        if (refs == null || refs.isEmpty()) {
            load(selectedRef);
            return;
        }

        final ArrayList<GitReference> refList = new ArrayList<>(
                refs.values());
        int checked = -1;
        if (selectedRef != null) {
            String ref = selectedRef.ref;
            for (int i = 0; i < refList.size(); i++) {
                String candidate = refList.get(i).ref;
                if (ref.equals(candidate)) {
                    checked = i;
                    break;
                } else if (ref.equals(RefUtils.getName(candidate))) {
                    checked = i;
                    break;
                }
            }
        }

        RefDialogFragment.show(activity, requestCode,
                activity.getString(R.string.select_ref), null, refList, checked);
    }
}
