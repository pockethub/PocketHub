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
package com.github.mobile.ui.ref;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.accounts.Account;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.core.ref.RefUtils;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.service.DataService;

/**
 * Dialog to select a branch or tag
 */
public class RefDialog {

    private static final String TAG = "RefDialog";

    private DataService service;

    private Map<String, Reference> refs;

    private final int requestCode;

    private final DialogFragmentActivity activity;

    private final IRepositoryIdProvider repository;

    /**
     * Create dialog helper to display refs
     *
     * @param activity
     * @param requestCode
     * @param repository
     * @param service
     */
    public RefDialog(final DialogFragmentActivity activity,
            final int requestCode, final IRepositoryIdProvider repository,
            final DataService service) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.repository = repository;
        this.service = service;
    }

    private void load(final Reference selectedRef) {
        new ProgressDialogTask<List<Reference>>(activity) {

            @Override
            public List<Reference> run(Account account) throws Exception {
                List<Reference> allRefs = service.getReferences(repository);
                Map<String, Reference> loadedRefs = new TreeMap<String, Reference>(
                        CASE_INSENSITIVE_ORDER);
                for (Reference ref : allRefs)
                    if (RefUtils.isValid(ref))
                        loadedRefs.put(ref.getRef(), ref);
                refs = loadedRefs;
                return allRefs;
            }

            @Override
            protected void onSuccess(List<Reference> all) throws Exception {
                super.onSuccess(all);

                show(selectedRef);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Exception loading references", e);
                ToastUtils.show(activity, e, string.error_refs_load);
            }

            @Override
            public void execute() {
                showIndeterminate(string.loading_refs);

                super.execute();
            }
        }.execute();
    }

    /**
     * Show dialog with given reference selected
     *
     * @param selectedRef
     */
    public void show(Reference selectedRef) {
        if (refs == null || refs.isEmpty()) {
            load(selectedRef);
            return;
        }

        final ArrayList<Reference> users = new ArrayList<Reference>(
                refs.values());
        int checked = -1;
        if (selectedRef != null)
            for (int i = 0; i < users.size(); i++)
                if (selectedRef.getRef().equals(users.get(i).getRef()))
                    checked = i;
        RefDialogFragment.show(activity, requestCode,
                activity.getString(string.select_ref), null, users, checked);
    }
}
