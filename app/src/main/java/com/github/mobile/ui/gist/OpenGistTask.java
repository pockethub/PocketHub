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
package com.github.mobile.ui.gist;

import static com.github.mobile.RequestCodes.GIST_VIEW;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;

/**
 * Task to load and open a Gist with an id
 */
public class OpenGistTask extends ProgressDialogTask<Gist> {

    private final String id;

    @Inject
    private GistStore store;

    /**
     * Create task
     *
     * @param context
     * @param gistId
     */
    public OpenGistTask(final Activity context, final String gistId) {
        super(context);

        id = gistId;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        dismissProgress();

        Context context = getContext();
        progress = new ProgressDialog(context);
        progress.setIndeterminate(true);
        progress.setMessage(context.getString(string.loading_gist));
        progress.show();

        execute();
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        super.onSuccess(gist);

        ((Activity) getContext()).startActivityForResult(ViewGistsActivity.createIntent(gist), GIST_VIEW);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }

    @Override
    protected Gist run() throws Exception {
        return store.refreshGist(id);
    }
}
