package com.github.pockethub.core;

import android.net.Uri;

import com.alorma.github.sdk.services.client.GithubClient;

import org.eclipse.egit.github.core.util.UrlUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PageIterator<V> implements Iterator<Collection<V>>, Iterable<Collection<V>>{

    protected GitHubRequest<List<V>> request;
    protected int nextPage;
    protected int lastPage;
    protected Uri next;
    protected Uri last;

    public PageIterator(GitHubRequest<List<V>> request, int nextPage) {
        this.request = request;
        this.nextPage = this.lastPage = nextPage;
        this.next = Uri.EMPTY;
    }

    protected int parsePageNumber(Uri uri) {
        if(uri != null && uri != Uri.EMPTY) {

            String param = uri.getQueryParameter("page");
            if(param != null && param.length() != 0) {
                try {
                    return Integer.parseInt(param);
                } catch (NumberFormatException var4) {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public int getNextPage() {
        return this.nextPage;
    }

    public int getLastPage() {
        return this.lastPage;
    }

    public boolean hasNext() {
        return this.nextPage == 0 || this.next != null;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

    public Collection<V> next() {
        if(!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            List resources = null;
            GithubClient client = request.execute(nextPage);
            Object response = client.executeSync();
            if(response != null)
                resources = (List) response;

            if(resources == null)
                resources = Collections.emptyList();

            ++this.nextPage;
            this.last = client.last;
            this.lastPage = parsePageNumber(last);
            this.next = client.next;
            this.nextPage = parsePageNumber(next);
            return (Collection<V>)resources;
        }
    }

    public GitHubRequest<List<V>> getRequest() {
        return this.request;
    }

    public Iterator<Collection<V>> iterator() {
        return this;
    }

    public interface GitHubRequest<V>{
        GithubClient<V> execute(int page);
    }
}
