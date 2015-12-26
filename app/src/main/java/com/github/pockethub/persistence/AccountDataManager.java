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
package com.github.pockethub.persistence;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.RequestFuture;
import com.github.pockethub.RequestReader;
import com.github.pockethub.RequestWriter;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.persistence.OrganizationRepositories.Factory;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manager cache for an account
 */
public class AccountDataManager {

    private static final String TAG = "AccountDataManager";

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Format version to bump if serialization format changes and cache should
     * be ignored
     */
    private static final int FORMAT_VERSION = 4;

    @Inject
    private Context context;

    @Inject
    private DatabaseCache dbCache;

    @Inject
    private Factory allRepos;

    @Inject
    private Organizations userAndOrgsResource;

    @Inject
    @Named("cacheDir")
    private File root;

    /**
     * @return context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Read data from file
     *
     * @param file
     * @return data
     */
    @SuppressWarnings("unchecked")
    private <V> V read(final File file) {
        long start = System.currentTimeMillis();
        long length = file.length();
        Object data = new RequestReader(file, FORMAT_VERSION).read();
        if (data != null)
            Log.d(TAG, MessageFormat.format(
                    "Cache hit to {0}, {1} ms to load {2} bytes",
                    file.getName(), (System.currentTimeMillis() - start),
                    length));
        return (V) data;
    }

    /**
     * Write data to file
     *
     * @param file
     * @param data
     * @return this manager
     */
    private AccountDataManager write(File file, Object data) {
        new RequestWriter(file, FORMAT_VERSION).write(data);
        return this;
    }

    /**
     * Query tables for columns
     *
     * @param helper
     * @param tables
     * @param columns
     * @return cursor
     */
    protected Cursor query(SQLiteOpenHelper helper, String tables,
            String[] columns) {
        return query(helper, tables, columns, null, null);
    }

    /**
     * Query tables for columns
     *
     * @param helper
     * @param tables
     * @param columns
     * @param selection
     * @param selectionArgs
     * @return cursor
     */
    protected Cursor query(SQLiteOpenHelper helper, String tables,
            String[] columns, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tables);
        return builder.query(helper.getReadableDatabase(), columns, selection,
                selectionArgs, null, null, null);
    }

    /**
     * Get organizations
     * <p/>
     * This method may perform file and/or network I/O and should never be
     * called on the UI-thread
     *
     * @param forceReload
     * @return list of user and Orgs
     * @throws IOException
     */
    public List<Organization> getOrgs(boolean forceReload) throws IOException {
        return forceReload ? dbCache.requestAndStore(userAndOrgsResource)
                : dbCache.loadOrRequest(userAndOrgsResource);
    }

    /**
     * Get repositories for given {@link User}
     * <p/>
     * This method may perform network I/O and should never be called on the
     * UI-thread
     *
     * @param user
     * @param forceReload
     *            if true, cached data will not be returned
     * @return list of repositories
     * @throws IOException
     */
    public List<Repo> getRepos(final User user, boolean forceReload)
            throws IOException {
        OrganizationRepositories resource = allRepos.under(user);
        return forceReload ? dbCache.requestAndStore(resource) : dbCache
                .loadOrRequest(resource);
    }

    /**
     * Get bookmarked issue filters
     * <p/>
     * This method may perform network I/O and should never be called on the
     * UI-thread
     *
     * @return non-null but possibly empty collection of issue filters
     */
    public Collection<IssueFilter> getIssueFilters() {
        final File cache = new File(root, "issue_filters.ser");
        Collection<IssueFilter> cached = read(cache);
        if (cached != null)
            return cached;
        return Collections.emptyList();
    }

    /**
     * Get bookmarked issue filters
     *
     * @param requestFuture
     */
    public void getIssueFilters(
            final RequestFuture<Collection<IssueFilter>> requestFuture) {
        new AuthenticatedUserTask<Collection<IssueFilter>>(context, EXECUTOR) {

            @Override
            public Collection<IssueFilter> run(Account account)
                    throws Exception {
                return getIssueFilters();
            }

            @Override
            protected void onSuccess(Collection<IssueFilter> filters)
                    throws Exception {
                requestFuture.success(filters);
            }
        }.execute();
    }

    /**
     * Add issue filter to store
     * <p/>
     * This method may perform file I/O and should never be called on the
     * UI-thread
     *
     * @param filter
     */
    public void addIssueFilter(IssueFilter filter) {
        final File cache = new File(root, "issue_filters.ser");
        Collection<IssueFilter> filters = read(cache);
        if (filters == null)
            filters = new HashSet<>();
        if (filters.add(filter))
            write(cache, filters);
    }

    /**
     * Add issue filter to store
     *
     * @param filter
     * @param requestFuture
     */
    public void addIssueFilter(final IssueFilter filter,
            final RequestFuture<IssueFilter> requestFuture) {
        new AuthenticatedUserTask<IssueFilter>(context, EXECUTOR) {

            @Override
            public IssueFilter run(Account account) throws Exception {
                addIssueFilter(filter);
                return filter;
            }

            @Override
            protected void onSuccess(IssueFilter filter) throws Exception {
                requestFuture.success(filter);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception adding issue filter", e);
            }
        }.execute();
    }

    /**
     * Add issue filter from store
     * <p/>
     * This method may perform file I/O and should never be called on the
     * UI-thread
     *
     * @param filter
     */
    public void removeIssueFilter(IssueFilter filter) {
        final File cache = new File(root, "issue_filters.ser");
        Collection<IssueFilter> filters = read(cache);
        if (filters != null && filters.remove(filter))
            write(cache, filters);
    }

    /**
     * Remove issue filter from store
     *
     * @param filter
     * @param requestFuture
     */
    public void removeIssueFilter(final IssueFilter filter,
            final RequestFuture<IssueFilter> requestFuture) {
        new AuthenticatedUserTask<IssueFilter>(context, EXECUTOR) {

            @Override
            public IssueFilter run(Account account) throws Exception {
                removeIssueFilter(filter);
                return filter;
            }

            @Override
            protected void onSuccess(IssueFilter filter) throws Exception {
                requestFuture.success(filter);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception removing issue filter", e);
            }
        }.execute();
    }
}
