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

package com.github.pockethub.core;

import android.net.Uri;

import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.gitskarios.core.Pair;

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
            GithubListClient client = request.execute(nextPage);
            Object response = client.observable().toBlocking().first();
            if(response != null)
                resources = (List) ((Pair) response).first;

            if(resources == null)
                resources = Collections.emptyList();

            ++this.nextPage;
            this.last = client.last != null ? Uri.parse(client.last.toString()) : Uri.EMPTY;
            this.lastPage = parsePageNumber(last);
            this.next = client.next != null ? Uri.parse(client.next.toString()) : Uri.EMPTY;
            this.nextPage = parsePageNumber(next);
            return (Collection<V>) resources;
        }
    }

    public GitHubRequest<List<V>> getRequest() {
        return this.request;
    }

    public Iterator<Collection<V>> iterator() {
        return this;
    }

    public interface GitHubRequest<V>{
        GithubListClient<V> execute(int page);
    }
}
