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

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.github.mobile.util.MetricsUtils;

/**
 * Listener that manager gesture in activity
 */
public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    private Activity mActivity;
    private FlingBackType mType;
    private DisplayMetrics mDisplayMetrics;

    /**
     * the minimum values of the left edge of the starting point,use in leftEdge type
     */
    private float minXEdgeDistance;
    /**
     * minimum sliding distance in the x direction
     */
    private float minXDistance;
    /**
     * minimum sliding distance in the y direction
     */
    private float minYDistance;

    /**
     * FlingBackType
     */
    public static enum FlingBackType {
        /**
         * fling left(entire screen) to finish activity
         */
        left,
        /**
         * fling left edge to finish activity
         */
        leftEdge
    }

    public GestureListener(Activity activity) {
        this(activity, FlingBackType.left);
    }

    public GestureListener(Activity activity, FlingBackType flingBackType) {
        this.mActivity = activity;
        this.mType = flingBackType;

        mDisplayMetrics = MetricsUtils.getDisplayMetrics(activity);
        minXEdgeDistance = mDisplayMetrics.widthPixels / 15.0f;
        minXDistance = mDisplayMetrics.widthPixels / 20.0f;
        minYDistance = mDisplayMetrics.heightPixels / 30.0f;
    }

    public void setFlingBackType(FlingBackType flingBackType) {
        this.mType = flingBackType;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float xDistance = e2.getX() - e1.getX();
        float yDistance = e2.getY() - e1.getY();
        switch (mType) {
            case left:
                if (xDistance > minXDistance && yDistance < minYDistance && yDistance >
                    -minYDistance) {
                    mActivity.finish();
                }
                break;
            case leftEdge:
                if (xDistance > minXDistance && yDistance < minYDistance && yDistance >
                    -minYDistance && e1.getX() < minXEdgeDistance) {
                    mActivity.finish();
                }
                break;
            default:
                break;
        }
        return false;
    }
}
