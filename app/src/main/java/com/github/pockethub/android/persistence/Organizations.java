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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.organizations.OrganizationService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache of organization under an account
 */
public class Organizations implements PersistableResource<User> {

    private final Context context;

    /**
     * Create organizations cache with services to load from
     *
     * @param context
     */
    @Inject
    public Organizations(Context context) {
        this.context = context;
    }

    @Override
    public Cursor getCursor(SQLiteDatabase readableDatabase) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("orgs JOIN users ON (orgs.id = users.id)");
        return builder
                .query(readableDatabase, new String[] { "users.id",
                        "users.name", "users.avatarurl" }, null, null, null,
                        null, null);
    }

    @Override
    public User loadFrom(Cursor cursor) {
        return User.builder()
                .id(cursor.getInt(0))
                .login(cursor.getString(1))
                .avatarUrl(cursor.getString(2))
                .build();
    }

    @Override
    public void store(SQLiteDatabase db, List<User> orgs) {
        db.delete("orgs", null, null);
        if (orgs.isEmpty()) {
            return;
        }

        ContentValues values = new ContentValues(3);
        for (User user : orgs) {
            values.clear();

            values.put("id", user.id());
            db.replace("orgs", null, values);

            values.put("name", user.login());
            values.put("avatarurl", user.avatarUrl());
            db.replace("users", null, values);
        }
    }

    @Override
    public List<User> request() throws IOException {
        User user = ServiceGenerator.createService(context, UserService.class).getUser()
                .blockingGet()
                .body();

        List<User> all = getAllOrgs();
        all.add(user);
        return all;
    }

    private List<User> getAllOrgs() {
        List<User> repos = new ArrayList<>();
        int current = 1;
        int last = -1;

        while(current != last) {
            Page<User> page = ServiceGenerator.createService(context, OrganizationService.class)
                    .getMyOrganizations(current)
                    .blockingGet()
                    .body();

            repos.addAll(page.items());
            last = page.last() != null ? page.last() : -1;
            current = page.next() != null ? page.next() : -1;
        }

        return repos;
    }
}
