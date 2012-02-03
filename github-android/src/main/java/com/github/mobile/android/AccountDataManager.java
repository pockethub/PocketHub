package com.github.mobile.android;

import static org.eclipse.egit.github.core.User.TYPE_USER;
import android.content.Context;
import android.util.Log;

import com.github.mobile.android.issue.IssueFilter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.util.RoboAsyncTask;

/**
 * Manager cache for an account
 */
public class AccountDataManager {

    private static final String TAG = "GHDM";

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Format version to bump if serialization format changes and cache should be ignored
     */
    private static final int FORMAT_VERSION = 1;

    private static String digest(String value) {
        byte[] digested;
        try {
            digested = MessageDigest.getInstance("SHA-1").digest(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        String hashed = new BigInteger(1, digested).toString(16);
        int padding = 40 - hashed.length();
        if (padding == 0)
            return hashed;
        char[] zeros = new char[padding];
        Arrays.fill(zeros, '0');
        return new String(zeros) + hashed;
    }

    private final Context context;

    private final UserService users;

    private final OrganizationService orgs;

    private final RepositoryService repos;

    private final IssueService issues;

    private final File root;

    /**
     * Create manager storing data at given root folder
     *
     * @param context
     * @param root
     * @param users
     * @param orgs
     * @param repos
     * @param issues
     */
    public AccountDataManager(final Context context, final File root, UserService users, OrganizationService orgs,
            RepositoryService repos, IssueService issues) {
        this.context = context;
        this.root = root;
        this.users = users;
        this.orgs = orgs;
        this.repos = repos;
        this.issues = issues;
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
     * Get orgs.
     * <p>
     * This method may perform file and/or network I/O and should never be called on the UI-thread
     *
     * @return list of users
     * @throws IOException
     */
    public List<User> getOrgs() throws IOException {
        final File cache = new File(root, "orgs.ser");
        List<User> cached = read(cache);
        if (cached != null)
            return cached;

        List<User> loaded = new ArrayList<User>(orgs.getOrganizations());
        Collections.sort(loaded, new Comparator<User>() {

            public int compare(User u1, User u2) {
                return u1.getLogin().compareToIgnoreCase(u2.getLogin());
            }
        });
        loaded.add(0, users.getUser());
        write(cache, loaded);
        return loaded;
    }

    /**
     * Get orgs
     *
     * @param requestFuture
     */
    public void getOrgs(final RequestFuture<List<User>> requestFuture) {

        new RoboAsyncTask<List<User>>(context, EXECUTOR) {

            public List<User> call() throws Exception {
                return getOrgs();
            }

            protected void onSuccess(List<User> orgs) throws Exception {
                requestFuture.success(orgs);
            };
        }.execute();
    }

    /**
     * Get repositories for given {@link User}
     * <p>
     * This method may perform network I/O and should never be called on the UI-thread
     *
     * @see #getRepos(User, RequestFuture)
     * @param user
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepos(final User user) throws IOException {
        final File folder = new File(root, user.getLogin());
        final File cache = new File(folder, "repos.ser");
        List<Repository> cached = read(cache);
        if (cached != null)
            return cached;

        List<Repository> loaded;
        if (!TYPE_USER.equals(user.getType()))
            loaded = repos.getOrgRepositories(user.getLogin());
        else if (user.getLogin().equals(repos.getClient().getUser()))
            loaded = repos.getRepositories();
        else
            loaded = repos.getRepositories(user.getLogin());
        Collections.sort(loaded, new Comparator<Repository>() {

            public int compare(Repository r1, Repository r2) {
                return r1.getName().compareToIgnoreCase(r2.getName());
            }
        });
        write(cache, loaded);
        return loaded;
    }

    /**
     * Get repositories for user
     *
     * @param user
     * @param requestFuture
     */
    public void getRepos(final User user, final RequestFuture<List<Repository>> requestFuture) {
        new RoboAsyncTask<List<Repository>>(context, EXECUTOR) {

            public List<Repository> call() throws Exception {
                return getRepos(user);
            }

            protected void onSuccess(List<Repository> repos) throws Exception {
                requestFuture.success(repos);
            };
        }.execute();
    }

    /**
     * Get repositories for user
     *
     * @param repository
     * @param filter
     * @param requestFuture
     */
    public void getIssues(final IRepositoryIdProvider repository, final Map<String, String> filter,
            final RequestFuture<List<Issue>> requestFuture) {
        final File folder = new File(root, repository.generateId());
        new RoboAsyncTask<List<Issue>>(context, EXECUTOR) {

            public List<Issue> call() throws Exception {
                StringBuilder filterId = new StringBuilder();
                for (Entry<String, String> entry : filter.entrySet())
                    filterId.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
                File cache = new File(folder, digest(filterId.toString()));

                List<Issue> cached = read(cache);
                if (cached != null)
                    return cached;

                List<Issue> loaded = issues.getIssues(repository, filter);
                write(cache, loaded);
                return loaded;
            }

            protected void onSuccess(List<Issue> repos) throws Exception {
                requestFuture.success(repos);
            };
        }.execute();
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
}
