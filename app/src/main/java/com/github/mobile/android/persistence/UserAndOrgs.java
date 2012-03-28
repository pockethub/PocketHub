package com.github.mobile.android.persistence;

import static com.google.common.collect.Lists.newArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.UserService;

public class UserAndOrgs implements PersistableResource<User> {

    private final UserService users;

    private final OrganizationService orgs;

    @Inject
    public UserAndOrgs(OrganizationService orgs, UserService users) {
        this.orgs = orgs;
        this.users = users;
    }

    @Override
    public Cursor getCursor(SQLiteDatabase readableDatabase) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("orgs JOIN users ON (orgs.id = users.id)");
        return builder.query(readableDatabase, new String[] { "users.id", "users.name", "users.avatarurl" },
                null, null, null, null, null);
    }

    @Override
    public User loadFrom(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(0));
        user.setLogin(cursor.getString(1));
        user.setAvatarUrl(cursor.getString(2));
        return user;
    }

    @Override
    public void store(SQLiteDatabase db, List<User> userAndOrgs) {
        db.delete("orgs", null, null);
        for (User user : userAndOrgs) {
            ContentValues values = new ContentValues(3);

            values.put("id", user.getId());
            db.replace("orgs", null, values);

            values.put("name", user.getLogin());
            values.put("avatarurl", user.getAvatarUrl());
            db.replace("users", null, values);
        }
    }

    @Override
    public List<User> request() throws IOException {
        List<User> userAndOrgs = newArrayList(orgs.getOrganizations());
        userAndOrgs.add(0, users.getUser());
        return userAndOrgs;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
