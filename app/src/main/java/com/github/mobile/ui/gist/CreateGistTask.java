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

import android.app.Activity;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import java.util.Collections;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Task to create a {@link Gist}
 */
public class CreateGistTask extends ProgressDialogTask<Gist> {

    private static final String TAG = "CreateGistTask";

    @Inject
    private GistService service;

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
    public CreateGistTask(Activity activity, String description, boolean isPublic, String name, String content) {
        super(activity);

        this.description = description;
        this.isPublic = isPublic;
        this.name = name;
        this.content = content;
    }

    @Override
    public Gist run() throws Exception {
        Gist gist = new Gist();
        gist.setDescription(description);
        gist.setPublic(isPublic);

        GistFile file = new GistFile();
        file.setContent(content);
        file.setFilename(name);
        gist.setFiles(Collections.singletonMap(name, file));

        return service.createGist(gist);
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
        dismissProgress();
        showIndeterminate(string.creating_gist);

        execute();
    }
}