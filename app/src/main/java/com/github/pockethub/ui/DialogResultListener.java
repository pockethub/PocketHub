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

/**
 * Listener that dialogs results are delivered too
 */
public interface DialogResultListener {

    /**
     * Callback for a dialog finishing and delivering a result
     *
     * @param requestCode
     * @param resultCode
     *            result such as {@link Activity#RESULT_CANCELED} or
     *            {@link Activity#RESULT_OK}
     * @param arguments
     */
    void onDialogResult(int requestCode, int resultCode, Bundle arguments);
}
