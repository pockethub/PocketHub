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
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.gists.PublicGistsClient;
import com.github.pockethub.R;
import com.github.pockethub.core.PageIterator;
import com.github.pockethub.core.gist.GistStore;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.service.GistService;

import java.util.Collection;
import java.util.List;

import static com.github.pockethub.RequestCodes.GIST_VIEW;

/**
 * Task to open a random Gist
 */
public class RandomGistTask extends ProgressDialogTask<Gist> {

    private static final String TAG = "RandomGistTask";

    @Inject
    private GistService service;

    @Inject
    private GistStore store;

    /**
     * Create task
     *
     * @param context
     */
    public RandomGistTask(final Activity context) {
        super(context);
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        showIndeterminate(R.string.random_gist);

        execute();
    }

    @Override
    protected Gist run(Account account) throws Exception {
        PageIterator<Gist> pages = new PageIterator<>(new PageIterator.GitHubRequest<List<Gist>>() {
            @Override
            public GithubListClient<List<Gist>> execute(int page) {
                return new PublicGistsClient(1);
            }
        }, 1);
        pages.next();
        int randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));

        Collection<Gist> gists = pages.getRequest().execute(randomPage).observable().toBlocking().first().first;

        // Make at least two tries since page numbers are volatile
        if (gists.isEmpty()) {
            randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));
            gists = pages.getRequest().execute(randomPage).observable().toBlocking().first().first;
        }

        if (gists.isEmpty())
            throw new IllegalArgumentException(getContext().getString(
                    R.string.no_gists_found));

        return store.addGist(gists.iterator().next());
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        super.onSuccess(gist);

        ((Activity) getContext()).startActivityForResult(
                GistsViewActivity.createIntent(gist), GIST_VIEW);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception opening random Gist", e);
        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
