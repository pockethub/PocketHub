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
package com.github.pockethub.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;

/**
 * Base class for activity tests
 *
 * @param <T>
 */
public abstract class ActivityTest<T extends Activity> extends
        ActivityInstrumentationTestCase2<T> {

    /**
     * @param activityClass
     */
    public ActivityTest(Class<T> activityClass) {
        super(activityClass);
    }

    /**
     * Verify activity was created successfully
     */
    public void testActivityIsCreated() {
        assertNotNull(getActivity());
    }

    /**
     * Get edit text with id
     *
     * @param id
     * @return edit text
     */
    protected EditText editText(final int id) {
        return (EditText) view(id);
    }

    /**
     * Get view with id
     *
     * @param id
     * @return edit text
     */
    protected View view(final int id) {
        assertNotNull(getActivity());
        View view = getActivity().findViewById(id);
        assertNotNull(view);
        return view;
    }

    /**
     * Send focus to view
     *
     * @param view
     * @throws Throwable
     */
    protected void focus(final View view) throws Throwable {
        ui(new Runnable() {

            public void run() {
                view.requestFocus();
            }
        });
    }

    /**
     * Run runnable on ui thread
     *
     * @param runnable
     * @throws Throwable
     */
    protected void ui(Runnable runnable) throws Throwable {
        runTestOnUiThread(runnable);
    }

    /**
     * Send text
     *
     * @param text
     */
    protected void send(final String text) {
        getInstrumentation().waitForIdleSync();
        getInstrumentation().sendStringSync(text);
        getInstrumentation().waitForIdleSync();
    }
}
