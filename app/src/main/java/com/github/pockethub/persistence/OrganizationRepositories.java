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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.Permissions;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.orgs.OrgsReposClient;
import com.alorma.github.sdk.services.repos.UserReposClient;
import com.alorma.github.sdk.services.repos.WatchedReposClient;
import com.github.pockethub.accounts.GitHubAccount;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cache of repositories under a given organization
 */
public class OrganizationRepositories implements
        PersistableResource<Repo> {

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
        OrganizationRepositories under(Organization org);
    }

    private final Organization org;

    private final Context context;

    private final Provider<GitHubAccount> accountProvider;

    /**
     * Create repositories cache for a given organization
     *
     * @param orgs
     * @param context
     * @param accountProvider
     */
    @Inject
    public OrganizationRepositories(@Assisted Organization orgs, Context context,
            Provider<GitHubAccount> accountProvider) {
        this.org = orgs;
        this.context = context;
        this.accountProvider = accountProvider;
    }

    @Override
    public Cursor getCursor(SQLiteDatabase readableDatabase) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("repos JOIN users ON (repos.ownerId = users.id)");
        return builder.query(readableDatabase, new String[] {
                "repos.repoId", "repos.name", "users.id", "users.name", "users.avatarurl",
                "repos.private", "repos.fork", "repos.description", "repos.forks",
                "repos.watchers", "repos.language", "repos.hasIssues", "repos.mirrorUrl",
                "repos.permissions_admin", "repos.permissions_pull", "repos.permissions_push" },
                "repos.orgId=?",
                new String[] { Integer.toString(org.id) }, null, null,
                null);
    }

    @Override
    public Repo loadFrom(Cursor cursor) {
        Repo repo = new Repo();
        repo.id = cursor.getLong(0);
        repo.name = cursor.getString(1);

        User owner = new User();
        owner.id = cursor.getInt(2);
        owner.login = cursor.getString(3);
        owner.avatar_url = cursor.getString(4);
        repo.owner = owner;

        repo.isPrivate = cursor.getInt(5) == 1;
        repo.fork = cursor.getInt(6) == 1;
        repo.description = cursor.getString(7);
        repo.forks_count = cursor.getInt(8);
        repo.watchers_count = cursor.getInt(9);
        repo.language = cursor.getString(10);
        repo.has_issues = cursor.getInt(11) == 1;
        repo.mirror_url = cursor.getString(12);

        repo.permissions = new Permissions();
        repo.permissions.admin = cursor.getInt(13) == 1;
        repo.permissions.pull = cursor.getInt(14) == 1;
        repo.permissions.push = cursor.getInt(15) == 1;

        return repo;
    }

    @Override
    public void store(SQLiteDatabase db, List<Repo> repos) {
        db.delete("repos", "orgId=?",
                new String[] { Integer.toString(org.id) });
        if (repos.isEmpty())
            return;

        ContentValues values = new ContentValues(12);
        for (Repo repo : repos) {
            values.clear();

            User owner = repo.owner;
            values.put("repoId", repo.id);
            values.put("name", repo.name);
            values.put("orgId", org.id);
            values.put("ownerId", owner.id);
            values.put("private", repo.isPrivate ? 1 : 0);
            values.put("fork", repo.fork ? 1 : 0);
            values.put("description", repo.description);
            values.put("forks", repo.forks_count);
            values.put("watchers", repo.watchers_count);
            values.put("language", repo.language);
            values.put("hasIssues", repo.has_issues ? 1 : 0);
            values.put("mirrorUrl", repo.mirror_url);
            values.put("permissions_admin", repo.canAdmin() ? 1 : 0);
            values.put("permissions_pull", repo.canPull() ? 1 : 0);
            values.put("permissions_push", repo.canPush() ? 1 : 0);

            db.replace("repos", null, values);

            values.clear();

            values.put("id", owner.id);
            values.put("name", owner.login);
            values.put("avatarurl", owner.avatar_url);
            db.replace("users", null, values);
        }
    }

    @Override
    public List<Repo> request() throws IOException {
        if (isAuthenticatedUser()) {
            Set<Repo> all = new TreeSet<>(
                    new Comparator<Repo>() {

                        @Override
                        public int compare(final Repo repo1,
                                final Repo repo2) {
                            final long id1 = repo1.id;
                            final long id2 = repo2.id;
                            if (id1 > id2)
                                return 1;
                            if (id1 < id2)
                                return -1;
                            return 0;
                        }
                    });

            all.addAll(new UserReposClient().observable().toBlocking().first().first);
            all.addAll(new WatchedReposClient(org.login, null, 0).observable().toBlocking().first().first);
            return new ArrayList<>(all);
        } else
            return new OrgsReposClient(org.login, null, 0).observable().toBlocking().first().first;
    }

    private boolean isAuthenticatedUser() {
        return org.login.equals(accountProvider.get().getUsername());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + org.login + ']';
    }
}
