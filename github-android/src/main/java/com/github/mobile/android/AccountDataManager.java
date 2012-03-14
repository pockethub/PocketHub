package com.github.mobile.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.github.mobile.android.issue.IssueFilter;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.util.RoboAsyncTask;

/**
 * Manager cache for an account
 */
public class AccountDataManager {

    private static class CacheHelper extends SQLiteOpenHelper {

        /**
         * @param context
         */
        public CacheHelper(Context context) {
            super(context, "cache.db", null, 1);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE orgs (id INTEGER PRIMARY KEY);");
            db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, avatarurl TEXT);");
            db.execSQL("CREATE TABLE repos (id INTEGER PRIMARY KEY, orgId INTEGER, name TEXT, ownerId INTEGER);");
            db.execSQL("CREATE TABLE avatars (id TEXT PRIMARY KEY, avatar BLOB);");
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

    private static final String TAG = "GHDM";

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Format version to bump if serialization format changes and cache should be ignored
     */
    private static final int FORMAT_VERSION = 2;

    private final Context context;

    private final UserService users;

    private final OrganizationService orgs;

    private final RepositoryService repos;

    private final File root;

    /**
     * Create manager storing data at given root folder
     *
     * @param context
     * @param root
     * @param users
     * @param orgs
     * @param repos
     */
    public AccountDataManager(final Context context, final File root, final UserService users,
            final OrganizationService orgs, final RepositoryService repos) {
        this.context = context;
        this.root = root;
        this.users = users;
        this.orgs = orgs;
        this.repos = repos;
    }

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
     * <p>
     * This method may perform file and/or network I/O and should never be called on the UI-thread
     *
     * @return list of users
     * @throws IOException
     */
    public List<User> getOrgs() throws IOException {
        SQLiteOpenHelper helper = new CacheHelper(context);
        try {
            Cursor cursor = query(helper, "orgs JOIN users ON (orgs.id = users.id)", //
                    new String[] { "users.id", "users.name", "users.avatarurl" });
            try {
                if (cursor.moveToFirst()) {
                    List<User> cached = new ArrayList<User>();
                    do {
                        User user = new User();
                        user.setId(cursor.getInt(0));
                        user.setLogin(cursor.getString(1));
                        user.setAvatarUrl(cursor.getString(2));
                        cached.add(user);
                    } while (cursor.moveToNext());
                    return cached;
                }
            } finally {
                cursor.close();
            }

            List<User> loaded = new ArrayList<User>(orgs.getOrganizations());
            loaded.add(0, users.getUser());

            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                db.beginTransaction();
                db.delete("orgs", null, null);
                for (User user : loaded) {
                    ContentValues values = new ContentValues(3);
                    values.put("id", user.getId());
                    db.replace("orgs", null, values);
                    values.put("name", user.getLogin());
                    values.put("avatarurl", user.getAvatarUrl());
                    db.replace("users", null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
            return loaded;
        } finally {
            helper.close();
        }
    }

    /**
     * Get repositories for given {@link User}
     * <p>
     * This method may perform network I/O and should never be called on the UI-thread
     *
     * @param user
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepos(final User user) throws IOException {
        SQLiteOpenHelper helper = new CacheHelper(context);
        try {
            Cursor cursor = query(helper, "repos JOIN users ON (repos.ownerId = users.id)", //
                    new String[] { "repos.id, repos.name", "users.id", "users.name", "users.avatarurl" }, //
                    "repos.orgId=?", new String[] { Integer.toString(user.getId()) });
            try {
                if (cursor.moveToFirst()) {
                    List<Repository> repos = new ArrayList<Repository>();
                    do {
                        Repository repo = new Repository();
                        repo.setId(cursor.getLong(0));
                        repo.setName(cursor.getString(1));
                        User owner = new User();
                        owner.setId(cursor.getInt(2));
                        owner.setLogin(cursor.getString(3));
                        owner.setAvatarUrl(cursor.getString(4));
                        repo.setOwner(owner);
                        repos.add(repo);
                    } while (cursor.moveToNext());
                    return repos;
                }
            } finally {
                cursor.close();
            }

            List<Repository> loaded;
            if (user.getLogin().equals(repos.getClient().getUser()))
                loaded = repos.getRepositories();
            else
                loaded = repos.getOrgRepositories(user.getLogin());

            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                db.beginTransaction();
                db.delete("repos", "orgId=?", new String[] { Integer.toString(user.getId()) });
                for (Repository repo : loaded) {
                    User owner = repo.getOwner();

                    ContentValues values = new ContentValues(3);
                    values.put("name", repo.getName());
                    values.put("orgId", user.getId());
                    values.put("ownerId", owner.getId());
                    db.replace("repos", null, values);

                    values.clear();
                    values.put("id", owner.getId());
                    values.put("name", owner.getLogin());
                    values.put("avatarurl", owner.getAvatarUrl());
                    db.replace("users", null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }

            return loaded;
        } finally {
            helper.close();
        }
    }

    /**
     * Get avatar for login
     *
     * @param login
     * @return avatar blob
     */
    public byte[] getAvatar(final String login) {
        SQLiteOpenHelper helper = new CacheHelper(context);
        Cursor cursor = query(helper, "avatars", new String[] { "avatar" }, "id='" + login + "'", null);
        try {
            return cursor.moveToFirst() ? cursor.getBlob(0) : null;
        } finally {
            cursor.close();
            helper.close();
        }
    }

    /**
     * Set avatar for login
     *
     * @param login
     * @param image
     */
    public void setAvatar(final String login, final byte[] image) {
        SQLiteOpenHelper helper = new CacheHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues(2);
            values.put("id", login);
            values.put("avatar", image);
            db.replace("avatars", null, values);
        } finally {
            db.close();
            helper.close();
        }
    }

    /**
     * Get bookmarked issue filters
     * <p>
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
            };
        }.execute();
    }

    /**
     * Add issue filter to store
     * <p>
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
            };

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception adding issue filter", e);
            };
        }.execute();
    }

    /**
     * Add issue filter from store
     * <p>
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
            };

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception removing issue filter", e);
            };
        }.execute();
    }
}
