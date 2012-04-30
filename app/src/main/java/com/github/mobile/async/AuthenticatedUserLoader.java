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

import com.github.mobile.AsyncLoader;
import com.github.mobile.accounts.GitHubAccountScope;
import com.google.inject.Inject;

import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

/**
 * Enforces that user is logged in before work on the background thread commences.
 *
 * @param <D>
 */
public abstract class AuthenticatedUserLoader<D> extends AsyncLoader<D> {

    @Inject
    private ContextScope contextScope;

    @Inject
    private GitHubAccountScope gitHubAccountScope;

    /**
     * Activity using this loader
     */
    @Inject
    protected Activity activity;

    /**
     * Create loader for context
     *
     * @param context
     */
    public AuthenticatedUserLoader(final Context context) {
        super(context);

        RoboGuice.injectMembers(context, this);
    }

    @Override
    public final D loadInBackground() {
        gitHubAccountScope.enterWith(activity);
        try {
            contextScope.enter(getContext());
            try {
                return load();
            } finally {
                contextScope.exit(getContext());
            }
        } finally {
            gitHubAccountScope.exit();
        }
    }

    /**
     * Load data
     *
     * @return data
     */
    public abstract D load();
}