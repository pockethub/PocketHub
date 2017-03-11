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

package com.github.pockethub.android.util;

import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

public class InfoUtils {

    public static Repository createRepoFromUrl(String url) {
        if (url == null || url.length() == 0) {
            return null;
        }
        String owner = null;
        String name = null;
        for (String segment : url.split("/")) //$NON-NLS-1$
        {
            if (segment.length() > 0) {
                if (owner == null) {
                    owner = segment;
                } else if (name == null) {
                    name = segment;
                } else {
                    break;
                }
            }
        }

        if (owner != null && owner.length() > 0 && name != null && name.length() > 0) {
            return createRepoFromData(owner, name);
        } else {
            return null;
        }
    }

    public static String createRepoId(Repository repo) {
        if(repo.name().contains("/")) {
            return repo.name();
        } else {
            return createRepoId(repo.owner().login(), repo.name());
        }
    }

    public static String createRepoId(String owner, String name) {
        return owner + "/" + name;
    }

    public static Repository createRepoFromData(String repoOwner, String repoName) {
        User user = User.builder().login(repoOwner).build();
        return Repository.builder().owner(user).name(repoName).build();
    }
}
