package com.github.mobile.android.repo;

import static com.github.mobile.android.util.SharedPreferencesUtil.savePrefsFrom;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;

/**
 * Helper for obtaining the recently viewed repositories for under a given user or organization
 */
public class RecentReposHelper {

    /**
     * The maximum number of recent repos to store - this is the total across all different orgs.
     */
    private static final int MAX_RECENT_REPOS = 20;

    private static final String PREF_RECENT_REPOS = "recentRepos";

    private static final Joiner JOINER = Joiner.on(",");

    private static final Splitter SPLITTER = Splitter.on(",");

    private static final Function<Repository, String> REPO_NAME = new Function<Repository, String>() {
        public String apply(Repository repo) {
            return repo.getName();
        }
    };

    private static final Function<Repository, String> REPO_ID = new Function<Repository, String>() {
        public String apply(Repository repo) {
            return repo.generateId();
        }
    };

    private static Ordering<Repository> REPO_NAME_ORDER = Ordering.from(CASE_INSENSITIVE_ORDER).onResultOf(REPO_NAME);

    private LinkedList<String> recentRepos;

    @Inject
    private SharedPreferences sharedPreferences;

    /**
     * Load recent repos from store.
     */
    public void load() {
        recentRepos = newLinkedList(SPLITTER.split(sharedPreferences.getString(PREF_RECENT_REPOS, "")));
    }

    /**
     * Load the recent-repo list, add the supplied repository to it's head and store the list.
     * <p/>
     * It's expected that this would be called just once for a Repo-viewing activity.
     *
     * @param repo
     */
    public void storeRepoVisit(IRepositoryIdProvider repo) {
        if (repo == null)
            return;

        String repoId = repo.generateId();

        if (TextUtils.isEmpty(repoId))
            return;

        load();
        recentRepos.remove(repoId);
        recentRepos.addFirst(repoId);

        while (recentRepos.size() > MAX_RECENT_REPOS)
            recentRepos.removeLast();

        savePrefsFrom(sharedPreferences.edit().putString(PREF_RECENT_REPOS, JOINER.join(recentRepos)));
    }

    /**
     * Find recently viewed repos amongst the list of supplied repos. The most recently viewed repos will head the
     * resulting list, ordered by recency, followed by the other repos in the supplied list.
     *
     * @param fullRepoList the full set of repos that will be displayed
     * @param numberOfTopRecentReposToShow the max num repos to show as 'recent' - prioritising the <em>most</em> recent
     * @return value-object with the full sorted list of repos (headed by recents), plus the ids of the recent repos
     */
    public RecentRepos recentReposFrom(List<Repository> fullRepoList, int numberOfTopRecentReposToShow) {
        Map<String, Repository> reposById = uniqueIndex(fullRepoList, REPO_ID);

        Set<String> allRepoIds = reposById.keySet();
        Function<String, Repository> idToRepo = Functions.forMap(reposById);

        LinkedHashSet<String> topRecentRepoIds = topRecentReposIn(allRepoIds, numberOfTopRecentReposToShow);
        Set<String> otherRepoIds = difference(allRepoIds, topRecentRepoIds);

        Iterable<Repository> topRecentRepos = transform(topRecentRepoIds, idToRepo);
        Iterable<Repository> otherReposSortedByName = REPO_NAME_ORDER.sortedCopy(transform(otherRepoIds, idToRepo));

        return new RecentRepos(newArrayList(concat(topRecentRepos, otherReposSortedByName)), topRecentRepoIds);
    }

    private LinkedHashSet<String> topRecentReposIn(Set<String> fullRepoList, int numberOfTopRecentReposToShow) {
        return newLinkedHashSet(limit(filter(recentRepos, in(fullRepoList)), numberOfTopRecentReposToShow));
    }

    /**
     * Value-object holding a sorted list of repos and the ids of the most recently-view repos.
     */
    public static class RecentRepos implements Serializable {
        private static final long serialVersionUID = 5216432701122989971L;

        public final List<Repository> fullRepoListHeadedByTopRecents;
        public final LinkedHashSet<String> topRecentRepoIds;

        public RecentRepos(List<Repository> fullRepoListHeadedByTopRecents, LinkedHashSet<String> topRecentRepoIds) {
            this.fullRepoListHeadedByTopRecents = fullRepoListHeadedByTopRecents;
            this.topRecentRepoIds = topRecentRepoIds;
        }
    }

}
