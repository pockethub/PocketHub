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
package com.github.pockethub.android;

import android.content.Context;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.IOException;

/**
 * Provide GitHub-API related services
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    User currentUser(Context context) throws IOException {
        return ServiceGenerator.createService(context, UserService.class)
                .getUser()
                .blockingGet()
                .body();
    }
}
