package com.github.mobile.android.repo;

import android.content.Context;
import android.text.TextUtils;

import com.github.mobile.android.RequestReader;
import com.github.mobile.android.RequestWriter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;

/**
 * Helper for obtaining the recently viewed repositories for under a given user or organization
 */
public class RecentReposHelper {

    private static final String FILE_RECENT_REPOS = "recent_repos.ser";

    private static final int MAX_RECENT_REPOS = 5;

    private static final int VERSION_RECENT_REPOS = 1;

    private final LinkedHashSet<String> recentRepos;

    private final Context context;

    private final User user;

    /**
     * Create helper scoped to given user
     *
     * @param context
     * @param user
     */
    public RecentReposHelper(final Context context, final User user) {
        this.context = context;
        this.user = user;

        LinkedHashSet<String> loaded = new RequestReader(getRecentReposFile(), VERSION_RECENT_REPOS).read();
        if (loaded == null)
            loaded = new LinkedHashSet<String>();
        recentRepos = loaded;
        trimRecentRepos();
    }

    /**
     * Add repository to recent list
     *
     * @param repo
     * @return this helper
     */
    public RecentReposHelper add(final IRepositoryIdProvider repo) {
        return repo != null ? add(repo.generateId()) : this;
    }

    /**
     * Add id to recent list
     *
     * @param repoId
     * @return this helper
     */
    public RecentReposHelper add(final String repoId) {
        if (!TextUtils.isEmpty(repoId)) {
            recentRepos.remove(repoId);
            if (recentRepos.add(repoId))
                trimRecentRepos();
        }
        return this;
    }

    private void trimRecentRepos() {
        if (recentRepos.size() <= MAX_RECENT_REPOS)
            return;

        Iterator<String> iter = recentRepos.iterator();
        while (iter.hasNext()) {
            iter.next();
            if (recentRepos.size() > MAX_RECENT_REPOS)
                iter.remove();
        }
    }

    private File getRecentReposFile() {
        return context.getFileStreamPath(user.getLogin() + '_' + FILE_RECENT_REPOS);
    }

    /**
     * Persist recent list
     *
     * @return this helper
     */
    public RecentReposHelper save() {
        new RequestWriter(getRecentReposFile(), VERSION_RECENT_REPOS).write(recentRepos);
        return this;
    }

    /**
     * Is the given repository id contained in the recent list?
     *
     * @param repoId
     * @return true if recent, false otherwise
     */
    public boolean contains(String repoId) {
        return !TextUtils.isEmpty(repoId) && recentRepos.contains(repoId);
    }
}
