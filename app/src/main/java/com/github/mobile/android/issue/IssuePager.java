package com.github.mobile.android.issue;

import com.github.mobile.android.ResourcePager;

import org.eclipse.egit.github.core.Issue;

/**
 * Helper class for showing more and more pages of issues
 */
public abstract class IssuePager extends ResourcePager<Issue> {

    /**
     * Store to add loaded issues to
     */
    protected final IssueStore store;

    /**
     * Create issue pager
     *
     * @param store
     */
    public IssuePager(final IssueStore store) {
        this.store = store;
    }

    @Override
    protected Issue register(Issue resource) {
        return store.addIssue(resource);
    }

    @Override
    protected Object getId(Issue resource) {
        return resource.getId();
    }
}
