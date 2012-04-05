package com.github.mobile.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.github.mobile.android.authenticator.GitHubAccount;
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
        return builder.query(readableDatabase, new String[] { "repos.id, repos.name",
                "users.id", "users.name", "users.avatarurl" }, "repos.orgId=?",
                new String[] { Integer.toString(userOrOrg
                .getId()) }, null, null, null);
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
        return repo;
    }

    @Override
    public void store(SQLiteDatabase db, List<Repository> repos) {
        db.delete("repos", "orgId=?", new String[] { Integer.toString(userOrOrg.getId()) });
        for (Repository repo : repos) {
            User owner = repo.getOwner();

            ContentValues values = new ContentValues(3);
            values.put("name", repo.getName());
            values.put("orgId", userOrOrg.getId());
            values.put("ownerId", owner.getId());
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
