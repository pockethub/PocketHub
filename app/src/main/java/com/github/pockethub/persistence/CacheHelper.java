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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;

/**
 * Helper class to create & upgrade database cache tables
 */
public class CacheHelper extends SQLiteOpenHelper {

    /**
     * Version constant to increment when the database should be rebuilt
     */
    private static final int VERSION = 9;

    /**
     * Name of database file
     */
    private static final String NAME = "cache.db";

    /**
     * @param context
     */
    @Inject
    public CacheHelper(final Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE orgs (id INTEGER PRIMARY KEY);");
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, avatarurl TEXT);");
        db.execSQL("CREATE TABLE repos (id INTEGER PRIMARY KEY, repoId INTEGER, orgId INTEGER, " +
                "name TEXT, ownerId INTEGER, private INTEGER, fork INTEGER, description TEXT, " +
                "forks INTEGER, watchers INTEGER, language TEXT, hasIssues INTEGER, " +
                "mirrorUrl TEXT, permissions_admin INTEGER, permissions_pull INTEGER, " +
                "permissions_push INTEGER);");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS orgs");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS repos");
        onCreate(db);
    }
}
