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
package com.github.mobile.core.gist;

import android.content.Context;
import android.util.Log;

import com.github.mobile.async.AuthenticatedUserTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.inject.ContextScopedProvider;

/**
 * Task to star a {@link Gist}
 */
public class StarGistTask extends AuthenticatedUserTask<Gist> {

    private static final String TAG = "StarGistTask";

    @Inject
    private ContextScopedProvider<GistService> serviceProvider;

    private final String id;

    /**
     * Create task to star a {@link Gist}
     *
     * @param context
     * @param id
     */
    public StarGistTask(final Context context, final String id) {
        super(context);

        this.id = id;
    }

    @Override
    public Gist run() throws Exception {
        serviceProvider.get(getContext()).starGist(id);
        return null;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        Log.d(TAG, "Exception starring gist", e);
    }
}
