package com.github.mobile.android.util;

import static org.eclipse.egit.github.core.RepositoryId.createFromUrl;
import android.content.Intent;

import java.io.Serializable;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

/**
 * Helper for creating intents
 */
public class GitHubIntents {

    /**
     * Prefix for all intents created
     */
    public static final String INTENT_PREFIX = "com.github.mobile.android.";

    /**
     * Prefix for all extra data added to intents
     */
    public static final String INTENT_EXTRA_PREFIX = INTENT_PREFIX + "extra.";

    /**
     * Repository handle
     */
    public static final String EXTRA_REPOSITORY = INTENT_EXTRA_PREFIX + "REPOSITORY";

    /**
     * Repository name
     */
    public static final String EXTRA_REPOSITORY_NAME = INTENT_EXTRA_PREFIX + "REPOSITORY_NAME";

    /**
     * Repository owner
     */
    public static final String EXTRA_REPOSITORY_OWNER = INTENT_EXTRA_PREFIX + "REPOSITORY_OWNER";

    /**
     * Issue number
     */
    public static final String EXTRA_ISSUE_NUMBER = INTENT_EXTRA_PREFIX + "ISSUE_NUMBER";

    /**
     * Gist id
     */
    public static final String EXTRA_GIST_ID = INTENT_EXTRA_PREFIX + "GIST_ID";

    /**
     * Gist handle
     */
    public static final String EXTRA_GIST = INTENT_EXTRA_PREFIX + "GIST";

    /**
     * Gist file handle
     */
    public static final String EXTRA_GIST_FILE = INTENT_EXTRA_PREFIX + "GIST_FILE";

    /**
     * User handle
     */
    public static final String EXTRA_USER = INTENT_EXTRA_PREFIX + "USER";

    /**
     * Issue filter handle
     */
    public static final String EXTRA_ISSUE_FILTER = INTENT_EXTRA_PREFIX + "ISSUE_FILTER";

    /**
     * Resolve the {@link RepositoryId} referenced by the given intent
     *
     * @param intent
     * @return repository id
     */
    public static RepositoryId repoFrom(Intent intent) {
        String repoName = intent.getStringExtra(EXTRA_REPOSITORY_NAME);
        String repoOwner = intent.getStringExtra(EXTRA_REPOSITORY_OWNER);
        return RepositoryId.create(repoOwner, repoName);
    }

    /**
     * Builder for generating an intent configured with extra data such as an issue, repository, or gist
     */
    public static class Builder {

        private final Intent intent;

        /**
         * Create builder with suffix
         *
         * @param actionSuffix
         */
        public Builder(String actionSuffix) {
            intent = new Intent(INTENT_PREFIX + actionSuffix); // actionSuffix = e.g. "repos.VIEW"
        }

        /**
         * Add repository id to intent being built up
         *
         * @param repositoryId
         * @return this builder
         */
        public Builder repo(RepositoryId repositoryId) {
            return add(EXTRA_REPOSITORY_NAME, repositoryId.getName()).add(EXTRA_REPOSITORY_OWNER,
                    repositoryId.getOwner());
        }

        /**
         * Add repository to intent being built up
         *
         * @param repository
         * @return this builder
         */
        public Builder repo(Repository repository) {
            return add(EXTRA_REPOSITORY, repository);
        }

        /**
         * Add issue to intent being built up
         *
         * @param issue
         * @return this builder
         */
        public Builder issue(Issue issue) {
            return repo(createFromUrl(issue.getHtmlUrl())).add(EXTRA_ISSUE_NUMBER, issue.getNumber());
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
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, Serializable value) {
            intent.putExtra(fieldName, value);
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
