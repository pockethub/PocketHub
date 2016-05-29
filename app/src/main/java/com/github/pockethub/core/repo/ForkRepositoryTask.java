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

package com.github.pockethub.core.repo;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.repo.actions.ForkRepoClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.InfoUtils;

/**
 * Task to fork a repository
 */
public class ForkRepositoryTask extends ProgressDialogTask<Repo> {

    private static final String TAG = "ForkRepositoryTask";

    private final Repo repo;

    /**
     * Create task for context and id provider
     *
     * @param context
     * @param repo
     */
    public ForkRepositoryTask(Context context, Repo repo) {
        super(context);

        this.repo = repo;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        showIndeterminate(R.string.forking_repository);

        execute();
    }

    @Override
    protected Repo run(Account account) throws Exception {
        return new ForkRepoClient(InfoUtils.createRepoInfo(repo)).observable().toBlocking().first();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception forking repository", e);
    }
}
