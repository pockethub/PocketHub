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
