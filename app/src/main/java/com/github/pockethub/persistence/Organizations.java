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
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.services.orgs.GetOrgsClient;
import com.alorma.github.sdk.services.user.GetAuthUserClient;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache of organization under an account
 */
public class Organizations implements PersistableResource<Organization> {

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
        User user = new User();
        user.id = cursor.getInt(0);
        user.login = cursor.getString(1);
        user.avatar_url = cursor.getString(2);
        return user;
    }

    @Override
    public void store(SQLiteDatabase db, List<Organization> orgs) {
        db.delete("orgs", null, null);
        if (orgs.isEmpty())
            return;

        ContentValues values = new ContentValues(3);
        for (Organization user : orgs) {
            values.clear();

            values.put("id", user.id);
            db.replace("orgs", null, values);

            values.put("name", user.login);
            values.put("avatarurl", user.avatar_url);
            db.replace("users", null, values);
        }
    }

    @Override
    public List<Organization> request() throws IOException {
        User user = new GetAuthUserClient().observable().toBlocking().first();
        List<Organization> orgs = new GetOrgsClient(null).observable().toBlocking().first().first;
        List<Organization> all = new ArrayList<>(orgs.size() + 1);
        all.add(user);
        all.addAll(orgs);
        return all;
    }
}
