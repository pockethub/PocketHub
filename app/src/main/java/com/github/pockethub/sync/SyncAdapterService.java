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
package com.github.pockethub.sync;

import android.content.Intent;
import android.os.IBinder;

import com.google.inject.Inject;

import roboguice.inject.ContextScopedProvider;
import roboguice.service.RoboService;

/**
 * Sync adapter service
 */
public class SyncAdapterService extends RoboService {

    @Inject
    private ContextScopedProvider<SyncAdapter> syncAdapterProvider;

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapterProvider.get(this).getSyncAdapterBinder();
    }
}
