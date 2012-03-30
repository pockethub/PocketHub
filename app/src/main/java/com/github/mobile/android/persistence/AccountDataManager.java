package com.github.mobile.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.github.mobile.android.RequestFuture;
import com.github.mobile.android.RequestReader;
import com.github.mobile.android.RequestWriter;
import com.github.mobile.android.issue.IssueFilter;
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

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.util.RoboAsyncTask;

/**
 * Manager cache for an account
 */
public class AccountDataManager {

    public static class CacheHelper extends SQLiteOpenHelper {

        /**
         * @param context
         */
        @Inject
        public CacheHelper(Context context) {
            super(context, "cache.db", null, 1);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE orgs (id INTEGER PRIMARY KEY);");
            db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, avatarurl TEXT);");
            db.execSQL("CREATE TABLE repos (id INTEGER PRIMARY KEY, orgId INTEGER, name TEXT, ownerId INTEGER);");
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS orgs");
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS repos");
            db.execSQL("DROP TABLE IF EXISTS avatars");
            onCreate(db);
        }
    }

    private static final String TAG = "ADM";

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Format version to bump if serialization format changes and cache should be ignored
     */
    private static final int FORMAT_VERSION = 2;

    private
    @Inject
    Context context;
    private
    @Inject
    DBCache dbCache;
    private
    @Inject
    AllReposForUserOrOrg.Factory allRepos;
    private
    @Inject
    UserAndOrgs userAndOrgsResource;

    private
    @Inject
    @Named("cacheDir")
    File root;


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
    private <V> V read(File file) {
        long start = System.currentTimeMillis();
        long length = file.length();
        V data = new RequestReader(file, FORMAT_VERSION).read();
        if (data != null)
            Log.d(TAG,
                    MessageFormat.format("Cache hit to {0}, {1} ms to load {2} bytes", file.getName(),
                            (System.currentTimeMillis() - start), length));
        return data;
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
    protected Cursor query(SQLiteOpenHelper helper, String tables, String[] columns) {
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
    protected Cursor query(SQLiteOpenHelper helper, String tables, String[] columns, String selection,
                           String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tables);
        return builder.query(helper.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
    }

    /**
     * Get organizations
     * <p/>
     * This method may perform file and/or network I/O and should never be called on the UI-thread
     *
     * @return list of user and Orgs
     * @throws IOException
     */
    public List<User> getOrgs() throws IOException {
        return dbCache.loadOrRequest(userAndOrgsResource);
    }

    /**
     * Get repositories for given {@link User}
     * <p/>
     * This method may perform network I/O and should never be called on the UI-thread
     *
     * @param user
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepos(final User user) throws IOException {
        return dbCache.loadOrRequest(allRepos.under(user));
    }

    /**
     * Get bookmarked issue filters
     * <p/>
     * This method may perform network I/O and should never be called on the UI-thread
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
    public void getIssueFilters(final RequestFuture<Collection<IssueFilter>> requestFuture) {
        new RoboAsyncTask<Collection<IssueFilter>>(context, EXECUTOR) {

            public Collection<IssueFilter> call() throws Exception {
                return getIssueFilters();
            }

            protected void onSuccess(Collection<IssueFilter> filters) throws Exception {
                requestFuture.success(filters);
            }

            ;
        }.execute();
    }

    /**
     * Add issue filter to store
     * <p/>
     * This method may perform file I/O and should never be called on the UI-thread
     *
     * @param filter
     */
    public void addIssueFilter(IssueFilter filter) {
        final File cache = new File(root, "issue_filters.ser");
        Collection<IssueFilter> filters = read(cache);
        if (filters == null)
            filters = new HashSet<IssueFilter>();
        if (filters.add(filter))
            write(cache, filters);
    }

    /**
     * Add issue filter to store
     *
     * @param filter
     * @param requestFuture
     */
    public void addIssueFilter(final IssueFilter filter, final RequestFuture<IssueFilter> requestFuture) {
        new RoboAsyncTask<IssueFilter>(context, EXECUTOR) {

            public IssueFilter call() throws Exception {
                addIssueFilter(filter);
                return filter;
            }

            protected void onSuccess(IssueFilter filter) throws Exception {
                requestFuture.success(filter);
            }

            ;

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception adding issue filter", e);
            }

            ;
        }.execute();
    }

    /**
     * Add issue filter from store
     * <p/>
     * This method may perform file I/O and should never be called on the UI-thread
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
    public void removeIssueFilter(final IssueFilter filter, final RequestFuture<IssueFilter> requestFuture) {
        new RoboAsyncTask<IssueFilter>(context, EXECUTOR) {

            public IssueFilter call() throws Exception {
                removeIssueFilter(filter);
                return filter;
            }

            protected void onSuccess(IssueFilter filter) throws Exception {
                requestFuture.success(filter);
            }

            ;

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception removing issue filter", e);
            }

            ;
        }.execute();
    }
}
