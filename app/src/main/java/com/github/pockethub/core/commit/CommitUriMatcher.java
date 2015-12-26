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
package com.github.pockethub.core.commit;

import android.net.Uri;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.repo.RepositoryUtils;

import java.util.List;

/**
 * Parses a {@link CommitMatch} from a {@link Uri}
 */
public class CommitUriMatcher {

    /**
     * Attempt to parse a {@link CommitMatch} from the given {@link Uri}
     *
     * @param uri
     * @return {@link CommitMatch} or null if unparseable
     */
    public static CommitMatch getCommit(Uri uri) {
        List<String> segments = uri.getPathSegments();
        if (segments == null)
            return null;
        if (segments.size() < 4)
            return null;
        if (!"commit".equals(segments.get(2)))
            return null;

        String repoOwner = segments.get(0);
        if (!RepositoryUtils.isValidOwner(repoOwner))
            return null;

        String repoName = segments.get(1);
        if (!RepositoryUtils.isValidRepo(repoName))
            return null;

        String commit = segments.get(3);
        if (!CommitUtils.isValidCommit(commit))
            return null;

        Repo repository = new Repo();
        User owner = new User();
        owner.login = repoOwner;
        repository.name = repoName;
        repository.owner = owner;
        return new CommitMatch(repository, commit);
    }
}
