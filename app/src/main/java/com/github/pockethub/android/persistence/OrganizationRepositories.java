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
package com.github.pockethub.android.persistence;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.github.pockethub.android.core.PageIterator;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Permissions;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.activity.WatchingService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Response;

/**
 * Cache of repositories under a given organization
 */
public class OrganizationRepositories implements
        PersistableResource<Repository> {

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

    private final Context context;

    private final Provider<Account> accountProvider;

    /**
     * Create repositories cache for a given organization
     *
     * @param orgs
     * @param context
     * @param accountProvider
     */
    @Inject
    public OrganizationRepositories(@Assisted User orgs, Context context,
            Provider<Account> accountProvider) {
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
                new String[] { Integer.toString(org.id()) }, null, null,
                null);
    }

    @Override
    public Repository loadFrom(Cursor cursor) {
        User owner = User.builder()
                .login(cursor.getString(3))
                .id(cursor.getInt(2))
                .avatarUrl(cursor.getString(4))
                .build();

        Permissions permissions = Permissions.builder()
                .admin(cursor.getInt(13) == 1)
                .push(cursor.getInt(14) == 1)
                .pull(cursor.getInt(15) == 1)
                .build();

        return Repository.builder()
                .owner(owner)
                .name(cursor.getString(1))
                .id(cursor.getLong(0))
                .isPrivate(cursor.getInt(5) == 1)
                .isFork(cursor.getInt(6) == 1)
                .description(cursor.getString(7))
                .forksCount(cursor.getInt(8))
                .watchersCount(cursor.getInt(9))
                .language(cursor.getString(10))
                .hasIssues(cursor.getInt(11) == 1)
                .mirrorUrl(cursor.getString(12))
                .permissions(permissions)
                .build();
    }

    @Override
    public void store(SQLiteDatabase db, List<Repository> repos) {
        db.delete("repos", "orgId=?",
                new String[] { Integer.toString(org.id()) });
        if (repos.isEmpty()) {
            return;
        }

        ContentValues values = new ContentValues(12);
        for (Repository repo : repos) {
            values.clear();

            User owner = repo.owner();
            values.put("repoId", repo.id());
            values.put("name", repo.name());
            values.put("orgId", org.id());
            values.put("ownerId", owner.id());
            values.put("private", repo.isPrivate() ? 1 : 0);
            values.put("fork", repo.isFork() ? 1 : 0);
            values.put("description", repo.description());
            values.put("forks", repo.forksCount());
            values.put("watchers", repo.watchersCount());
            values.put("language", repo.language());
            values.put("hasIssues", repo.hasIssues() ? 1 : 0);
            values.put("mirrorUrl", repo.mirrorUrl());
            values.put("permissions_admin", repo.permissions().admin() ? 1 : 0);
            values.put("permissions_pull", repo.permissions().pull() ? 1 : 0);
            values.put("permissions_push", repo.permissions().push() ? 1 : 0);

            db.replace("repos", null, values);

            values.clear();

            values.put("id", owner.id());
            values.put("name", owner.login());
            values.put("avatarurl", owner.avatarUrl());
            db.replace("users", null, values);
        }
    }

    @Override
    public List<Repository> request() throws IOException {
        if (isAuthenticatedUser()) {
            Set<Repository> all = new TreeSet<>((repo1, repo2) -> {
                final long id1 = repo1.id();
                final long id2 = repo2.id();
                if (id1 > id2) {
                    return 1;
                }
                if (id1 < id2) {
                    return -1;
                }
                return 0;
            });

            all.addAll(getAllItems(page ->
                    ServiceGenerator.createService(context, RepositoryService.class)
                            .getUserRepositories(page)));

            all.addAll(getAllItems(page ->
                    ServiceGenerator.createService(context, WatchingService.class)
                            .getWatchedRepositories(page)));
            return new ArrayList<>(all);
        } else {
            return getAllItems(page ->
                    ServiceGenerator.createService(context, RepositoryService.class)
                            .getOrganizationRepositories(org.login(), page));
        }
    }

    private List<Repository> getAllItems(PageIterator.GitHubRequest<Response<Page<Repository>>> request) {
        List<Repository> repos = new ArrayList<>();
        int current = 1;
        int last = -1;

        while(current != last) {
            Page<Repository> page = request.execute(current).blockingGet().body();
            repos.addAll(page.items());
            last = page.last() != null ? page.last() : -1;
            current = page.next() != null ? page.next() : -1;
        }

        return repos;
    }

    private boolean isAuthenticatedUser() {
        return org.login().equals(accountProvider.get().name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + org.login() + ']';
    }
}
