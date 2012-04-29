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
package com.github.mobile;

import static android.view.animation.Animation.INFINITE;
import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.anim;
import com.github.mobile.R.layout;

/**
 * Animation of a refresh menu item
 */
public class RefreshAnimation {

    private MenuItem refreshItem;

    /**
     * @param refreshItem
     * @return this animation
     */
    public RefreshAnimation setRefreshItem(MenuItem refreshItem) {
        this.refreshItem = refreshItem;
        return this;
    }

    /**
     * Start refresh animation
     *
     * @param activity
     */
    public void start(Activity activity) {
        if (activity == null || refreshItem == null)
            return;

        Animation rotation = AnimationUtils.loadAnimation(activity, anim.clockwise_refresh);
        rotation.setRepeatCount(INFINITE);
        View refreshView = activity.getLayoutInflater().inflate(layout.refresh_action_view, null);
        refreshView.startAnimation(rotation);
        refreshItem.setActionView(refreshView);
    }

    /**
     * Stop refresh animation
     */
    public void stop() {
        if (refreshItem == null || refreshItem.getActionView() == null)
            return;

        refreshItem.getActionView().clearAnimation();
        refreshItem.setActionView(null);
    }

}
