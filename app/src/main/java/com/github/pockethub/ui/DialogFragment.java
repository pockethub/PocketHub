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
package com.github.pockethub.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.pockethub.ui.roboactivities.RoboSupportFragment;

/**
 * Base fragment capable of receiving dialog callbacks
 */
public abstract class DialogFragment extends RoboSupportFragment implements
        DialogResultListener {

    /**
     * View finder bound to the value last specified to
     * {@link #onViewCreated(android.view.View, Bundle)}
     */
    protected ViewFinder finder;

    /**
     * Is this fragment usable from the UI-thread
     *
     * @return true if usable, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        // Intentionally left blank
    }

    /**
     * Get serializable extra from activity's intent
     *
     * @param name
     * @return extra
     */
    @SuppressWarnings("unchecked")
    protected <V extends Parcelable> V getParcelableExtra(final String name) {
        Activity activity = getActivity();
        if (activity != null)
            return (V) activity.getIntent().getParcelableExtra(name);
        else
            return null;
    }

    /**
     * Get string extra from activity's intent
     *
     * @param name
     * @return extra
     */
    protected String getStringExtra(final String name) {
        Activity activity = getActivity();
        if (activity != null)
            return activity.getIntent().getStringExtra(name);
        else
            return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        finder = new ViewFinder(view);
    }
}
