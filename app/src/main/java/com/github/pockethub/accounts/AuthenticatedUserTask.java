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
package com.github.pockethub.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.Executor;

import roboguice.inject.ContextScope;
import roboguice.util.RoboAsyncTask;

/**
 * Base task class that ensures an authenticated account exists before
 * {@link #run(Account)} is invoked
 *
 * @param <ResultT>
 */
public abstract class AuthenticatedUserTask<ResultT> extends
        RoboAsyncTask<ResultT> {

    @Inject
    private ContextScope contextScope;

    @Inject
    private AccountScope accountScope;

    @Inject
    private Activity activity;

    /**
     * Create asynchronous task that ensures a valid account is present when
     * executed
     *
     * @param context
     */
    protected AuthenticatedUserTask(final Context context) {
        super(context);
    }

    /**
     * Create asynchronous task that ensures a valid account is present when
     * executed
     *
     * @param context
     * @param executor
     */
    public AuthenticatedUserTask(final Context context, final Executor executor) {
        super(context, executor);
    }

    @Override
    public final ResultT call() throws Exception {
        final AccountManager manager = AccountManager.get(activity);
        final Account account = AccountUtils.getAccount(manager, activity);

        accountScope.enterWith(account, manager);
        try {
            contextScope.enter(getContext());
            try {
                return run(account);
            } catch (IOException e) {
                // Retry task if authentication failure occurs and account is
                // successfully updated
                if (AccountUtils.isUnauthorized(e)
                        && AccountUtils.updateAccount(account, activity))
                    return run(account);
                else
                    throw e;
            } finally {
                contextScope.exit(getContext());
            }
        } finally {
            accountScope.exit();
        }
    }

    /**
     * Execute task with an authenticated account
     *
     * @param account
     * @return result
     * @throws Exception
     */
    protected abstract ResultT run(Account account) throws Exception;
}
