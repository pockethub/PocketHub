package com.github.mobile.android.issue;

import android.util.Log;

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
    protected final Map<String, Issue> issues = new LinkedHashMap<String, Issue>();

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
        return new ArrayList<Issue>(issues.values());
    }

    /**
     * Get the next page of issues
     *
     * @return true if more pages
     * @throws IOException
     */
    public boolean next() throws IOException {
        Log.d("IP", "Page: " + page + " count: " + count);
        PageIterator<Issue> iterator = createIterator(page, -1);
        try {
            for (int i = 0; i < count && iterator.hasNext(); i++)
                for (Issue issue : iterator.next())
                    if (!issues.containsKey(issue.getUrl()))
                        issues.put(issue.getUrl(), issue);

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
