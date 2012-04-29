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
package com.github.mobile.async;

import android.app.Activity;
import android.content.Context;

import com.github.mobile.guice.GitHubAccountScope;
import com.google.inject.Inject;

import java.util.concurrent.Executor;

import roboguice.inject.ContextScope;
import roboguice.util.RoboAsyncTask;

/**
 * Enforces that user is logged in before work on the background thread commences.
 */
public abstract class AuthenticatedUserTask<ResultT> extends RoboAsyncTask<ResultT> {

    @Inject
    private ContextScope contextScope;

    @Inject
    private GitHubAccountScope gitHubAccountScope;

    @Inject
    private Activity activity;

    protected AuthenticatedUserTask(Context context) {
        super(context);
    }

    public AuthenticatedUserTask(Context context, Executor executor) {
        super(context, executor);
    }

    @Override
    public final ResultT call() throws Exception {
        gitHubAccountScope.enterWith(activity);
        try {
            contextScope.enter(getContext());
            try {
                return run();
            } finally {
                contextScope.exit(getContext());
            }
        } finally {
            gitHubAccountScope.exit();
        }
    }

    protected abstract ResultT run() throws Exception;
}
