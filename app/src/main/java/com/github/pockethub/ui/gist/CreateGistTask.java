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
import com.alorma.github.sdk.bean.dto.response.GistFile;
import com.alorma.github.sdk.bean.dto.response.GistFilesMap;
import com.alorma.github.sdk.services.gists.PublishGistClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to create a {@link Gist}
 */
public class CreateGistTask extends ProgressDialogTask<Gist> {

    private static final String TAG = "CreateGistTask";

    private final String description;

    private final boolean isPublic;

    private final String name;

    private final String content;

    /**
     * Create task that creates a {@link Gist}
     *
     * @param activity
     * @param description
     * @param isPublic
     * @param name
     * @param content
     */
    public CreateGistTask(Activity activity, String description,
            boolean isPublic, String name, String content) {
        super(activity);

        this.description = description;
        this.isPublic = isPublic;
        this.name = name;
        this.content = content;
    }

    @Override
    public Gist run(Account account) throws Exception {
        Gist gist = new Gist();
        gist.description = description;
        gist.isPublic = isPublic;

        GistFile file = new GistFile();
        file.content = content;
        file.filename = name;
        GistFilesMap map = new GistFilesMap();
        map.put(name, file);
        gist.files = map;

        return new PublishGistClient(gist).observable().toBlocking().first();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception creating Gist", e);
        ToastUtils.show((Activity) getContext(), e.getMessage());
    }

    /**
     * Create the {@link Gist} with the configured values
     */
    public void create() {
        showIndeterminate(R.string.creating_gist);

        execute();
    }
}
