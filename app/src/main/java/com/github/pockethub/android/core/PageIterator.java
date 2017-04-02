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

package com.github.pockethub.android.core;


import com.meisolsson.githubsdk.model.Page;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import io.reactivex.Single;
import retrofit2.Response;

public class PageIterator<V> implements Iterator<List>, Iterable<List> {

    protected GitHubRequest<Response<Page<V>>> request;
    protected Integer nextPage;
    protected Integer lastPage;

    public PageIterator(GitHubRequest<Response<Page<V>>> request, int nextPage) {
        this.request = request;
        this.nextPage = nextPage;
    }

    public int getNextPage() {
        return this.nextPage;
    }

    public int getLastPage() {
        return this.lastPage;
    }

    @Override
    public boolean hasNext() {
        return (this.nextPage != null && this.nextPage == 1) || this.lastPage != null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

    @Override
    public List<V> next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            Single<Response<Page<V>>> client = request.execute(nextPage);
            Page<V> response = client.blockingGet().body();

            ++this.nextPage;
            this.lastPage = response.last();
            this.nextPage = response.next();
            return response.items();
        }
    }

    public GitHubRequest<Response<Page<V>>> getRequest() {
        return this.request;
    }

    @Override
    public Iterator<List> iterator() {
        return this;
    }

    public interface GitHubRequest<V> {
        Single<V> execute(int page);
    }
}
