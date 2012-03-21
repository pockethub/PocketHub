package com.github.mobile.android.issue;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Helper class for showing more and more pages of issues
 */
public abstract class IssuePager {

    /**
     * Next page to request
     */
    protected int page = 1;

    /**
     * Number of pages to request
     */
    protected int count = 1;

    /**
     * All issues retrieved
     */
    protected final Map<Long, Issue> issues = new LinkedHashMap<Long, Issue>();

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

    /**
     * Reset the next page to be requested and clear the current issues
     *
     * @return this pager
     */
    public IssuePager reset() {
        count = Math.max(1, page - 1);
        page = 1;
        issues.clear();
        return this;
    }

    /**
     * Get issues
     *
     * @return issues
     */
    public List<Issue> getIssues() {
        return newArrayList(issues.values());
    }

    /**
     * Get the next page of issues
     *
     * @return true if more pages
     * @throws IOException
     */
    public boolean next() throws IOException {
        PageIterator<Issue> iterator = createIterator(page, -1);
        try {
            for (int i = 0; i < count && iterator.hasNext(); i++)
                for (Issue issue : iterator.next()) {
                    issue = store.addIssue(issue);
                    if (!issues.containsKey(issue.getId()))
                        issues.put(issue.getId(), issue);
                }

            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (NoSuchPageException e) {
            throw e.getCause();
        }
        return iterator.hasNext();
    }

    /**
     * Create iterator to return given page and size
     *
     * @param page
     * @param size
     * @return iterator
     */
    public abstract PageIterator<Issue> createIterator(final int page, final int size);
}
