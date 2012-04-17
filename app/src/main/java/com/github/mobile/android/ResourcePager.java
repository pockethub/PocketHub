package com.github.mobile.android;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Generic resource pager for elements with an id that can be paged
 *
 * @param <E>
 */
public abstract class ResourcePager<E> {

    /**
     * Next page to request
     */
    protected int page = 1;

    /**
     * Number of pages to request
     */
    protected int count = 1;

    /**
     * All resources retrieved
     */
    protected final Map<Object, E> resources = new LinkedHashMap<Object, E>();

    /**
     * Are more pages available?
     */
    protected boolean hasMore;

    /**
     * Reset the next page to be requested and clear the current resources
     *
     * @return this pager
     */
    public ResourcePager<E> reset() {
        count = Math.max(1, page - 1);
        page = 1;
        resources.clear();
        hasMore = true;
        return this;
    }

    /**
     * Get number of resources loaded into this pager
     *
     * @return number of resources
     */
    public int size() {
        return resources.size();
    }

    /**
     * Get resources
     *
     * @return resources
     */
    public List<E> getResources() {
        return newArrayList(resources.values());
    }

    /**
     * Get the next page of issues
     *
     * @return true if more pages
     * @throws IOException
     */
    public boolean next() throws IOException {
        boolean emptyPage = false;
        PageIterator<E> iterator = createIterator(page, -1);
        try {
            for (int i = 0; i < count && iterator.hasNext(); i++) {
                Collection<E> resourcePage = iterator.next();
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                for (E resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    resources.put(getId(resource), resource);
                }
            }
            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (NoSuchPageException e) {
            hasMore = false;
            throw e.getCause();
        }
        hasMore = iterator.hasNext() && !emptyPage;
        return hasMore;
    }

    /**
     * Are more pages available to request?
     *
     * @return true if the last call to {@link #next()} returned true, false otherwise
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Callback to register a fetched resource before it is stored in this pager
     * <p>
     * Sub-classes may override
     *
     * @param resource
     * @return resource
     */
    protected E register(final E resource) {
        return resource;
    }

    /**
     * Get id for resource
     *
     * @param resource
     * @return id
     */
    protected abstract Object getId(E resource);

    /**
     * Create iterator to return given page and size
     *
     * @param page
     * @param size
     * @return iterator
     */
    public abstract PageIterator<E> createIterator(final int page, final int size);
}
