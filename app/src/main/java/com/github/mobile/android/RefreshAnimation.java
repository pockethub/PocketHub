package com.github.mobile.android;

import static android.view.animation.Animation.INFINITE;
import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.anim;
import com.github.mobile.android.R.layout;

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
