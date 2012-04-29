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

import com.github.mobile.authenticator.GitHubAccount;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

public class AllReposForUserOrOrg implements PersistableResource<Repository> {

    public interface Factory {
        AllReposForUserOrOrg under(User userOrOrg);
    }

    private final User userOrOrg;
    private final RepositoryService repos;
    private final Provider<GitHubAccount> gitHubAccountProvider;

    @Inject
    public AllReposForUserOrOrg(@Assisted User userOrOrg, RepositoryService repos,
                                Provider<GitHubAccount> gitHubAccountProvider) {
        this.userOrOrg = userOrOrg;
        this.repos = repos;
        this.gitHubAccountProvider = gitHubAccountProvider;
    }

    @Override
    public Cursor getCursor(SQLiteDatabase readableDatabase) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("repos JOIN users ON (repos.ownerId = users.id)");
        return builder.query(readableDatabase, new String[] { "repos.id, repos.name", "users.id", "users.name",
                "users.avatarurl", "repos.private", "repos.fork" }, "repos.orgId=?",
                new String[] { Integer.toString(userOrOrg.getId()) }, null, null, null);
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

        return repo;
    }

    @Override
    public void store(SQLiteDatabase db, List<Repository> repos) {
        db.delete("repos", "orgId=?", new String[] { Integer.toString(userOrOrg.getId()) });
        for (Repository repo : repos) {
            User owner = repo.getOwner();

            ContentValues values = new ContentValues(5);
            values.put("name", repo.getName());
            values.put("orgId", userOrOrg.getId());
            values.put("ownerId", owner.getId());
            values.put("private", repo.isPrivate() ? 1 : 0);
            values.put("fork", repo.isFork() ? 1 : 0);
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
        if (userOrOrgIsAuthenticatedUser())
            return repos.getRepositories();
        else
            return repos.getOrgRepositories(userOrOrg.getLogin());
    }

    private boolean userOrOrgIsAuthenticatedUser() {
        return userOrOrg.getLogin().equals(gitHubAccountProvider.get().username);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + userOrOrg.getLogin() + "]";
    }
}
