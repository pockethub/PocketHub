/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.repo;

import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.content.Context;
import android.text.TextUtils;

import com.github.mobile.RequestReader;
import com.github.mobile.RequestWriter;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

import java.io.File;
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

    private static final String FILE_RECENT_REPOS = "recent_repos.ser";

    /**
     * The maximum number of recent repos to store - this is the total across all different orgs.
     */
    private static final int MAX_RECENT_REPOS = 20;

    private static final int VERSION_RECENT_REPOS = 2;

    public static final Function<Repository, String> REPO_NAME = new Function<Repository, String>() {
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

    private final LinkedList<String> recentRepos;

    private final Context context;

    /**
     * Create helper scoped to given user
     *
     * @param context
     */
    public RecentReposHelper(final Context context) {
        this.context = context;

        LinkedList<String> loaded = new RequestReader(getRecentReposFile(), VERSION_RECENT_REPOS).read();
        if (loaded == null)
            loaded = new LinkedList<String>();
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
            recentRepos.addFirst(repoId);
            trimRecentRepos();
        }
        return this;
    }

    private void trimRecentRepos() {
        while (recentRepos.size() > MAX_RECENT_REPOS)
            recentRepos.removeLast();
    }

    private File getRecentReposFile() {
        return context.getFileStreamPath(FILE_RECENT_REPOS);
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
