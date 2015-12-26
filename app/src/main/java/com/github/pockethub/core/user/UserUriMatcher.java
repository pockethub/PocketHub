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
package com.github.pockethub.core.user;

import android.net.Uri;

import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.repo.RepositoryUtils;

import java.util.List;

/**
 * Parses a {@link User} from a {@link Uri}
 */
public class UserUriMatcher {

    /**
     * Attempt to parse a {@link User} from the given {@link Uri}
     *
     * @param uri
     * @return {@link User} or null if unparseable
     */
    public static User getUser(Uri uri) {
        List<String> segments = uri.getPathSegments();
        if (segments == null)
            return null;
        if (segments.size() < 1)
            return null;

        String login = segments.get(0);
        if (!RepositoryUtils.isValidOwner(login))
            return null;
        User user = new User();
        user.login = login;
        return user;
    }
}
