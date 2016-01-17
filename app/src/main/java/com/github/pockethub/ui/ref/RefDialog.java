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
package com.github.pockethub.ui.ref;

import android.accounts.Account;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.GitReference;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.git.GetReferencesClient;
import com.github.pockethub.R;
import com.github.pockethub.core.ref.RefUtils;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog to select a branch or tag
 */
public class RefDialog {

    private static final String TAG = "RefDialog";

    private Map<String, GitReference> refs;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final Repo repository;

    /**
     * Create dialog helper to display refs
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public RefDialog(final DialogFragmentActivity activity,
            final int requestCode, final Repo repository) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
    }

    private void load(final GitReference selectedRef) {
        new ProgressDialogTask<List<GitReference>>(activity) {

            @Override
            public List<GitReference> run(Account account) throws Exception {
                List<GitReference> allRefs = new ArrayList<>();

                int page = 1;
                GetReferencesClient client = new GetReferencesClient(InfoUtils.createRepoInfo(repository), page);
                allRefs.addAll(client.observable().toBlocking().first().first);
                for(int i = client.nextPage; i < client.lastPage; i++) {
                    client = new GetReferencesClient(InfoUtils.createRepoInfo(repository), i);
                    allRefs.addAll(client.observable().toBlocking().first().first);
                }

                Map<String, GitReference> loadedRefs = new TreeMap<>(CASE_INSENSITIVE_ORDER);

                for (GitReference ref : allRefs)
                    if (RefUtils.isValid(ref))
                        loadedRefs.put(ref.ref, ref);

                refs = loadedRefs;
                return allRefs;
            }

            @Override
            protected void onSuccess(List<GitReference> all) throws Exception {
                super.onSuccess(all);

                show(selectedRef);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Exception loading references", e);
                ToastUtils.show(activity, e, R.string.error_refs_load);
            }

            @Override
            public void execute() {
                showIndeterminate(R.string.loading_refs);

                super.execute();
            }
        }.execute();
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
