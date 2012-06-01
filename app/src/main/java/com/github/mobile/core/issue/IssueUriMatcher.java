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
package com.github.mobile.core.issue;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.User;

/**
 * Parses a {@link RepositoryIssue} from a {@link Uri}
 */
public class IssueUriMatcher {

    /**
     * Parse a {@link RepositoryIssue} from a non-null {@link Uri}
     *
     * @param uri
     * @return {@link RepositoryIssue} or null if none found in given {@link Uri}
     */
    public static RepositoryIssue getIssue(Uri uri) {
        List<String> segments = uri.getPathSegments();
        if (segments == null)
            return null;
        if (segments.size() < 4)
            return null;
        if (!"issues".equals(segments.get(2)))
            return null;

        String repoOwner = segments.get(0);
        if (TextUtils.isEmpty(repoOwner))
            return null;

        String repoName = segments.get(1);
        if (TextUtils.isEmpty(repoName))
            return null;

        String number = segments.get(3);
        if (TextUtils.isEmpty(number))
            return null;

        int issueNumber;
        try {
            issueNumber = Integer.parseInt(number);
        } catch (NumberFormatException nfe) {
            return null;
        }
        if (issueNumber < 1)
            return null;

        Repository repo = new Repository();
        repo.setName(repoName);
        repo.setOwner(new User().setLogin(repoOwner));
        RepositoryIssue issue = new RepositoryIssue();
        issue.setRepository(repo);
        issue.setNumber(issueNumber);
        return issue;
    }
}
