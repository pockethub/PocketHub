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
package com.github.pockethub.ui.gist;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.github.pockethub.R;
import com.github.pockethub.api.DeleteGistClient;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.ToastUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Async task to delete a Gist
 */
public class DeleteGistTask extends ProgressDialogTask<Gist> {

    private static final String TAG = "DeleteGistTask";

    private final String id;

    /**
     * Create task
     *
     * @param context
     * @param gistId
     */
    public DeleteGistTask(final Activity context, final String gistId) {
        super(context);

        id = gistId;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        showIndeterminate(R.string.deleting_gist);

        execute();
    }

    @Override
    public Gist run(Account account) throws Exception {
        DeleteGistClient client = new DeleteGistClient(id);
        client.observable().toBlocking().first();
        return null;
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        super.onSuccess(gist);

        Activity activity = (Activity) getContext();
        activity.setResult(RESULT_OK);
        activity.finish();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception deleting Gist", e);
        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
