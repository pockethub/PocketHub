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
package com.github.mobile.util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.view.View;

/**
 * Utilities for dealing with {@link View} objects and their sub-classes
 */
public class ViewUtils {

    /**
     * Toggles the view's visibility between {@link View#VISIBLE} and
     * {@link View#GONE} depending on the given gone flag
     *
     * @param view
     * @param gone
     */
    public static void setGone(final View view, final boolean gone) {
        if (view == null)
            return;

        final int current = view.getVisibility();
        if (gone && current != GONE)
            view.setVisibility(GONE);
        else if (!gone && current != VISIBLE)
            view.setVisibility(VISIBLE);
    }
}
