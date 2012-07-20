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
package com.github.mobile.core.commit;

import com.github.mobile.core.UrlMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Matcher for commit URLs
 * <p>
 * This class is not thread-safe
 */
public class CommitUrlMatcher extends UrlMatcher {

    /**
     * Match for a commit in a repository
     */
    public static class CommitMatch {

        /**
         * Repository of commit
         */
        public final Repository repository;

        /**
         * SHA-1 of commit
         */
        public final String commit;

        private CommitMatch(final Repository repository, final String commit) {
            this.repository = repository;
            this.commit = commit;
        }
    }

    private static final String REGEX = "https?://.+/([^/]+)/([^/]+)/commit/([a-fA-F0-9]+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get commit match from URL
     *
     * @param url
     * @return commit match or null if the given URL wasn't a match
     */
    public CommitMatch getCommit(final String url) {
        if (!isMatch(url, matcher))
            return null;

        String owner = matcher.group(1);
        String name = matcher.group(2);
        String sha = matcher.group(3);
        Repository repo = new Repository();
        repo.setName(name);
        repo.setOwner(new User().setLogin(owner));
        return new CommitMatch(repo, sha);
    }
}
