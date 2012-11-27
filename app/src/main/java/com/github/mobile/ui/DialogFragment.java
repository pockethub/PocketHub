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
import android.os.Bundle;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import java.io.Serializable;

/**
 * Base fragment capable of receiving dialog callbacks
 */
public abstract class DialogFragment extends RoboSherlockFragment implements
        DialogResultListener {

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
    protected <V extends Serializable> V getSerializableExtra(final String name) {
        Activity activity = getActivity();
        if (activity != null)
            return (V) activity.getIntent().getSerializableExtra(name);
        else
            return null;
    }
}
