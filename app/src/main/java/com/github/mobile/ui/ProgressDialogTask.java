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
package com.github.mobile.ui;

import android.app.AlertDialog;
import android.content.Context;

import com.github.mobile.accounts.AuthenticatedUserTask;

import java.util.concurrent.Executor;

/**
 * Task that runs with a progress dialog at the foreground
 *
 * @param <E>
 */
public abstract class ProgressDialogTask<E> extends AuthenticatedUserTask<E> {

    /**
     * Progress dialog last displayed
     */
    protected AlertDialog progress;

    /**
     * @param context
     */
    protected ProgressDialogTask(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param executor
     */
    public ProgressDialogTask(Context context, Executor executor) {
        super(context, executor);
    }

    /**
     * Dismiss and clear progress dialog field
     */
    protected void dismissProgress() {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param message
     */
    protected void showIndeterminate(final CharSequence message) {
        dismissProgress();

        progress = LightProgressDialog.create(getContext(), message);
        progress.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param resId
     */
    protected void showIndeterminate(final int resId) {
        dismissProgress();

        progress = LightProgressDialog.create(getContext(), resId);
        progress.show();
    }

    /**
     * Sub-classes may override but should always call super to ensure the progress dialog is dismissed
     */
    @Override
    protected void onSuccess(E e) throws Exception {
        dismissProgress();
    }

    /**
     * Sub-classes may override but should always call super to ensure the progress dialog is dismissed
     */
    @Override
    protected void onException(Exception e) throws RuntimeException {
        dismissProgress();
    }

    /**
     * Get string from context resources
     *
     * @param resId
     * @return string
     */
    protected String getString(int resId) {
        return getContext().getString(resId);
    }
}
