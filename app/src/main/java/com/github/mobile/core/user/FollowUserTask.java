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
package com.github.mobile.core.user;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

/**
 * Task to follow a user
 */
public class FollowUserTask extends ProgressDialogTask<User> {

    private static final String TAG = "FollowUserTask";

    @Inject
    private UserService service;

    private final String login;
    private final boolean isFollowing;

    /**
     * Create task for context and login
     *
     * @param context
     * @param login
     * @param isFollowing
     */
    public FollowUserTask(Context context, String login, boolean isFollowing) {
        super(context);

        this.login = login;
        this.isFollowing = isFollowing;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        int progressMessage = isFollowing ? string.unfollowing_user : string.following_user;
        showIndeterminate(progressMessage);

        execute();
    }

    @Override
    protected User run(Account account) throws Exception {
        if (isFollowing) {
            service.unfollow(login);
        } else {
            service.follow(login);
        }

        return service.getUser(login);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception following/unfollowing user", e);
    }
}
