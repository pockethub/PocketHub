package com.github.mobile.android.issue;

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
     * All issues retrieved
     */
    protected final Map<String, Issue> issues = new LinkedHashMap<String, Issue>();

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
        PageIterator<Issue> iterator = createIterator(page, -1);
        try {
            for (Issue issue : iterator.next())
                if (!issues.containsKey(issue.getUrl()))
                    issues.put(issue.getUrl(), issue);
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
