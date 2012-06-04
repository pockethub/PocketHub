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
package com.github.mobile.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.github.mobile.accounts.GitHubAccount;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.WatcherService;

/**
 * Cache of repositories under a given organization
 */
public class OrganizationRepositories implements PersistableResource<Repository> {

    /**
     * Creation factory
     */
    public interface Factory {

        /**
         * Get repositories under given organization
         *
         * @param org
         * @return repositories
         */
        OrganizationRepositories under(User org);
    }

    private final User org;

    private final RepositoryService repos;

    private final WatcherService watcher;

    private final Provider<GitHubAccount> accountProvider;

    /**
     * Create repositories cache for a given organization
     *
     * @param orgs
     * @param repos
     * @param watcher
     * @param accountProvider
     */
    @Inject
    public OrganizationRepositories(@Assisted User orgs, RepositoryService repos, WatcherService watcher,
            Provider<GitHubAccount> accountProvider) {
        this.org = orgs;
        this.repos = repos;
        this.watcher = watcher;
        this.accountProvider = accountProvider;
    }

    @Override
    public Cursor getCursor(SQLiteDatabase readableDatabase) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("repos JOIN users ON (repos.ownerId = users.id)");
        return builder.query(readableDatabase, new String[] { "repos.repoId, repos.name", "users.id", "users.name",
                "users.avatarurl", "repos.private", "repos.fork", "repos.description", "repos.forks", "repos.watchers",
                "repos.language", "repos.hasIssues" }, "repos.orgId=?", new String[] { Integer.toString(org.getId()) },
                null, null, null);
    }

    @Override
    public Repository loadFrom(Cursor cursor) {
        Repository repo = new Repository();
        repo.setId(cursor.getLong(0));
        repo.setName(cursor.getString(1));

        User owner = new User();
        owner.setId(cursor.getInt(2));
        owner.setLogin(cursor.getString(3));
        owner.setAvatarUrl(cursor.getString(4));
        repo.setOwner(owner);

        repo.setPrivate(cursor.getInt(5) == 1);
        repo.setFork(cursor.getInt(6) == 1);
        repo.setDescription(cursor.getString(7));
        repo.setForks(cursor.getInt(8));
        repo.setWatchers(cursor.getInt(9));
        repo.setLanguage(cursor.getString(10));
        repo.setHasIssues(cursor.getInt(11) == 1);

        return repo;
    }

    @Override
    public void store(SQLiteDatabase db, List<Repository> repos) {
        db.delete("repos", "orgId=?", new String[] { Integer.toString(org.getId()) });
        for (Repository repo : repos) {
            User owner = repo.getOwner();
            ContentValues values = new ContentValues(11);

            values.put("repoId", repo.getId());
            values.put("name", repo.getName());
            values.put("orgId", org.getId());
            values.put("ownerId", owner.getId());
            values.put("private", repo.isPrivate() ? 1 : 0);
            values.put("fork", repo.isFork() ? 1 : 0);
            values.put("description", repo.getDescription());
            values.put("forks", repo.getForks());
            values.put("watchers", repo.getWatchers());
            values.put("language", repo.getLanguage());
            values.put("hasIssues", repo.isHasIssues() ? 1 : 0);
            db.replace("repos", null, values);

            values.clear();
            values.put("id", owner.getId());
            values.put("name", owner.getLogin());
            values.put("avatarurl", owner.getAvatarUrl());
            db.replace("users", null, values);
        }
    }

    @Override
    public List<Repository> request() throws IOException {
        if (isAuthenticatedUser()) {
            Set<Repository> all = new TreeSet<Repository>(new Comparator<Repository>() {

                public int compare(final Repository repo1, final Repository repo2) {
                    final long id1 = repo1.getId();
                    final long id2 = repo2.getId();
                    if (id1 > id2)
                        return 1;
                    if (id1 < id2)
                        return -1;
                    return 0;
                }
            });
            all.addAll(repos.getRepositories());
            all.addAll(watcher.getWatched());
            return new ArrayList<Repository>(all);
        } else
            return repos.getOrgRepositories(org.getLogin());
    }

    private boolean isAuthenticatedUser() {
        return org.getLogin().equals(accountProvider.get().username);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + org.getLogin() + "]";
    }
}
