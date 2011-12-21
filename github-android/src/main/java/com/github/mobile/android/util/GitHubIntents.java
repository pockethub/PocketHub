package com.github.mobile.android.util;

import static org.eclipse.egit.github.core.RepositoryId.createFromUrl;
import android.content.Intent;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;

public class GitHubIntents {

    private static final String TAG="GitHubIntents";

    public static final String INTENT_PREFIX = "com.github.mobile.android.";
    public static final String INTENT_EXTRA_PREFIX = INTENT_PREFIX+"extra.";

    public static final String EXTRA_REPOSITORY = INTENT_EXTRA_PREFIX+"REPOSITORY";
    public static final String EXTRA_REPOSITORY_OWNER = INTENT_EXTRA_PREFIX+"REPOSITORY_OWNER";
    public static final String EXTRA_ISSUE_NUMBER = INTENT_EXTRA_PREFIX+"ISSUE_NUMBER";

    public static RepositoryId repoFrom(Intent intent) {
        String repoName = intent.getStringExtra(EXTRA_REPOSITORY);
        String repoOwner = intent.getStringExtra(EXTRA_REPOSITORY_OWNER);
        return RepositoryId.create(repoOwner, repoName);
    }

    public static class Builder {

        private final Intent intent;

        public Builder(String actionSuffix) {
            intent = new Intent(INTENT_PREFIX +actionSuffix); // actionSuffix = e.g. "repos.VIEW"
        }

        public Builder repo(RepositoryId repositoryId) {
            return add(EXTRA_REPOSITORY, repositoryId.getName()).add(EXTRA_REPOSITORY_OWNER, repositoryId.getOwner());
        }

        public Builder issue(Issue issue) {
            return repo(createFromUrl(issue.getHtmlUrl())).add(EXTRA_ISSUE_NUMBER, issue.getNumber());
        }

        public Builder add(String fieldName, String value) {
            intent.putExtra(fieldName, value);
            return this;
        }
        
        public Builder add(String fieldName, int value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        public Intent toIntent() {
            return intent;
        }

    }
}
