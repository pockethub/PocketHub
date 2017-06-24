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

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.RxPageUtil;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.service.git.GitService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Dialog to select a branch or tag
 */
public class RefDialog {

    private static final String TAG = "RefDialog";

    private final int requestCode;

    private final BaseActivity activity;

    private final Single<List<GitReference>> refSingle;

    /**
     * Create dialog helper to display refs
     *
     * @param activity
     * @param requestCode
     * @param repository
     */
    public RefDialog(final BaseActivity activity,
            final int requestCode, final Repository repository) {
        this.activity = activity;
        this.requestCode = requestCode;

        PageIterator.GitHubRequest<Response<Page<GitReference>>> gitHubRequest = page -> ServiceGenerator
                .createService(activity, GitService.class)
                .getGitReferences(repository.owner().login(), repository.name(), page);

        refSingle = RxPageUtil.getAllPages(gitHubRequest, 1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .filter(RefUtils::isValid)
                .toSortedList((o1, o2) -> CASE_INSENSITIVE_ORDER.compare(o1.ref(), o2.ref()))
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading_refs))
                .cache();
    }

    /**
     * Show dialog with given reference selected
     *
     * @param selectedRef
     */
    public void show(GitReference selectedRef) {
        refSingle.subscribe(refs -> {
            int checked = -1;
            if (selectedRef != null) {
                String ref = selectedRef.ref();
                for (int i = 0; i < refs.size(); i++) {
                    String candidate = refs.get(i).ref();
                    if (ref.equals(candidate) || ref.equals(RefUtils.getName(candidate))) {
                        checked = i;
                        break;
                    }
                }
            }

            RefDialogFragment.show(activity, requestCode,
                    activity.getString(R.string.select_ref), null, new ArrayList<>(refs), checked);
        }, e -> {
            Log.d(TAG, "Exception loading references", e);
            ToastUtils.show(activity, e, R.string.error_refs_load);
        });
    }
}
