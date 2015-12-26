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
package com.github.pockethub;

import android.content.Intent;
import android.os.Parcelable;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GistFile;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.util.InfoUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Helper for creating intents
 */
public class Intents {

    /**
     * Prefix for all intents created
     */
    public static final String INTENT_PREFIX = "com.github.pockethub.";

    /**
     * Prefix for all extra data added to intents
     */
    public static final String INTENT_EXTRA_PREFIX = INTENT_PREFIX + "extra.";

    /**
     * Repo handle
     */
    public static final String EXTRA_REPOSITORY = INTENT_EXTRA_PREFIX
            + "REPOSITORY";

    /**
     * Repo ids collection handle
     */
    public static final String EXTRA_REPOSITORIES = INTENT_EXTRA_PREFIX
            + "REPOSITORIES";

    /**
     * Repo name
     */
    public static final String EXTRA_REPOSITORY_NAME = INTENT_EXTRA_PREFIX
            + "REPOSITORY_NAME";

    /**
     * Repo owner
     */
    public static final String EXTRA_REPOSITORY_OWNER = INTENT_EXTRA_PREFIX
            + "REPOSITORY_OWNER";

    /**
     * Issue number
     */
    public static final String EXTRA_ISSUE_NUMBER = INTENT_EXTRA_PREFIX
            + "ISSUE_NUMBER";

    /**
     * Issue handle
     */
    public static final String EXTRA_ISSUE = INTENT_EXTRA_PREFIX + "ISSUE";

    /**
     * Issue number collection handle
     */
    public static final String EXTRA_ISSUE_NUMBERS = INTENT_EXTRA_PREFIX
            + "ISSUE_NUMBERS";

    /**
     * Gist id
     */
    public static final String EXTRA_GIST_ID = INTENT_EXTRA_PREFIX + "GIST_ID";

    /**
     * List of Gist ids
     */
    public static final String EXTRA_GIST_IDS = INTENT_EXTRA_PREFIX
            + "GIST_IDS";

    /**
     * Gist handle
     */
    public static final String EXTRA_GIST = INTENT_EXTRA_PREFIX + "GIST";

    /**
     * Gist file handle
     */
    public static final String EXTRA_GIST_FILE = INTENT_EXTRA_PREFIX
            + "GIST_FILE";

    /**
     * User handle
     */
    public static final String EXTRA_USER = INTENT_EXTRA_PREFIX + "USER";

    /**
     * {@link ArrayList} handle of {@link User} objects
     */
    public static final String EXTRA_USERS = INTENT_EXTRA_PREFIX + "USERS";

    /**
     * Boolean value which indicates if a user is a collaborator on the repo
     */
    public static final String EXTRA_IS_COLLABORATOR = INTENT_EXTRA_PREFIX + "IS_COLLABORATOR";

    /**
     * Boolean value which indicates if a user is owner of the repo
     */
    public static final String EXTRA_IS_OWNER = INTENT_EXTRA_PREFIX + "IS_OWNER";

    /**
     * Issue filter handle
     */
    public static final String EXTRA_ISSUE_FILTER = INTENT_EXTRA_PREFIX
            + "ISSUE_FILTER";

    /**
     * Comment body
     */
    public static final String EXTRA_COMMENT_BODY = INTENT_EXTRA_PREFIX
            + "COMMENT_BODY";

    /**
     * Comments handle
     */
    public static final String EXTRA_COMMENTS = INTENT_EXTRA_PREFIX
            + "COMMENTS";

    /**
     * Comment handle
     */
    public static final String EXTRA_COMMENT = INTENT_EXTRA_PREFIX + "COMMENT";

    /**
     * Integer position
     */
    public static final String EXTRA_POSITION = INTENT_EXTRA_PREFIX
            + "POSITION";

    /**
     * Base commit name
     */
    public static final String EXTRA_BASE = INTENT_EXTRA_PREFIX + "BASE";

    /**
     * Base commit names
     */
    public static final String EXTRA_BASES = INTENT_EXTRA_PREFIX + "BASES";

    /**
     * Base commit name
     */
    public static final String EXTRA_HEAD = INTENT_EXTRA_PREFIX + "HEAD";

    /**
     * Handle to a {@link String} path
     */
    public static final String EXTRA_PATH = INTENT_EXTRA_PREFIX + "PATH";

    /**
     * Resolve the {@link Repo} referenced by the given intent
     *
     * @param intent
     * @return repository id
     */
    public static Repo repoFrom(Intent intent) {
        String repoName = intent.getStringExtra(EXTRA_REPOSITORY_NAME);
        String repoOwner = intent.getStringExtra(EXTRA_REPOSITORY_OWNER);
        return InfoUtils.createRepoFromData(repoOwner, repoName);
    }

    /**
     * Builder for generating an intent configured with extra data such as an
     * issue, repository, or gist
     */
    public static class Builder {

        private final Intent intent;

        /**
         * Create builder with suffix
         *
         * @param actionSuffix
         */
        public Builder(String actionSuffix) {
            // actionSuffix = e.g. "repos.VIEW"
            intent = new Intent(INTENT_PREFIX + actionSuffix);
        }

        /**
         * Add repository to intent being built up
         *
         * @param repository
         * @return this builder
         */
        public Builder repo(Repo repository) {
            return add(EXTRA_REPOSITORY, repository);
        }

        /**
         * Add issue to intent being built up
         *
         * @param issue
         * @return this builder
         */
        public Builder issue(Issue issue) {
            return repo(InfoUtils.createRepoFromUrl(issue.html_url)).add(EXTRA_ISSUE,
                    issue).add(EXTRA_ISSUE_NUMBER, issue.number);
        }

        /**
         * Add gist to intent being built up
         *
         * @param gist
         * @return this builder
         */
        public Builder gist(Gist gist) {
            return add(EXTRA_GIST, gist);
        }

        /**
         * Add gist id to intent being built up
         *
         * @param gist
         * @return this builder
         */
        public Builder gist(String gist) {
            return add(EXTRA_GIST_ID, gist);
        }

        /**
         * Add gist file to intent being built up
         *
         * @param file
         * @return this builder
         */
        public Builder gistFile(GistFile file) {
            return add(EXTRA_GIST_FILE, file);
        }

        /**
         * Add user to intent being built up
         *
         * @param user
         * @return this builder;
         */
        public Builder user(User user) {
            return add(EXTRA_USER, user);
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, String value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Add extra field data values to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, CharSequence[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, int value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, int[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, boolean[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, Serializable value) {
            intent.putExtra(fieldName, value);
            return this;
        }


        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, Parcelable value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, ArrayList<? extends Parcelable> value) {
            intent.putParcelableArrayListExtra(fieldName, value);
            return this;
        }

        /**
         * Get built intent
         *
         * @return intent
         */
        public Intent toIntent() {
            return intent;
        }
    }
}
