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

import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class SelectableLinkMovementMethod extends LinkMovementMethod {
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    private static SelectableLinkMovementMethod sInstance;
    private static Object FROM_BELOW = new NoCopySpan.Concrete();

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new SelectableLinkMovementMethod();
        }

        return sInstance;
    }


    /**
     * Enable arbitrary text selection for copy/paste
     */
    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }

    /**
     * Solve StringIndexOutOfBoundsException for some devices.
     * Without this, Selection.setSelection(...) will always return exception
     * and crashes the app.
     *
     * @param widget
     * @param buffer
     * @param event
     * @return <code>true</code> when the event is handles, <code>false</code> otherwise.
     */
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
        MotionEvent event) {
        return true;
    }


}
