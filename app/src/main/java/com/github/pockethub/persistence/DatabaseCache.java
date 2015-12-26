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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a PersistableResource, this class will take support loading/storing
 * it's data or requesting fresh data, as appropriate.
 */
public class DatabaseCache {

    private static final String TAG = "DatabaseCache";

    @Inject
    private Provider<CacheHelper> helperProvider;

    /**
     * Get writable database
     *
     * @param helper
     * @return writable database or null if it failed to create/open
     */
    protected SQLiteDatabase getWritable(SQLiteOpenHelper helper) {
        try {
            return helper.getWritableDatabase();
        } catch (SQLiteException e1) {
            // Make second attempt
            try {
                return helper.getWritableDatabase();
            } catch (SQLiteException e2) {
                return null;
            }
        }
    }

    /**
     * Get readable database
     *
     * @param helper
     * @return readable database or null if it failed to create/open
     */
    protected SQLiteDatabase getReadable(SQLiteOpenHelper helper) {
        try {
            return helper.getReadableDatabase();
        } catch (SQLiteException e1) {
            // Make second attempt
            try {
                return helper.getReadableDatabase();
            } catch (SQLiteException e2) {
                return null;
            }
        }
    }

    /**
     * Load or request given resources
     *
     * @param persistableResource
     * @return resource
     * @throws IOException
     */
    public <E> List<E> loadOrRequest(PersistableResource<E> persistableResource)
            throws IOException {
        SQLiteOpenHelper helper = helperProvider.get();
        try {
            List<E> items = loadFromDB(helper, persistableResource);
            if (items != null) {
                Log.d(TAG, "CACHE HIT: Found " + items.size() + " items for "
                        + persistableResource);
                return items;
            }
            return requestAndStore(helper, persistableResource);
        } finally {
            helper.close();
        }
    }

    /**
     * Request and store given resources
     *
     * @param persistableResource
     * @return resources
     * @throws IOException
     */
    public <E> List<E> requestAndStore(
            PersistableResource<E> persistableResource) throws IOException {
        SQLiteOpenHelper helper = helperProvider.get();
        try {
            return requestAndStore(helper, persistableResource);
        } finally {
            helper.close();
        }
    }

    private <E> List<E> requestAndStore(final SQLiteOpenHelper helper,
            final PersistableResource<E> persistableResource)
            throws IOException {
        final List<E> items = persistableResource.request();

        final SQLiteDatabase db = getWritable(helper);
        if (db == null)
            return items;

        db.beginTransaction();
        try {
            persistableResource.store(db, items);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return items;
    }

    private <E> List<E> loadFromDB(final SQLiteOpenHelper helper,
            final PersistableResource<E> persistableResource) {
        final SQLiteDatabase db = getReadable(helper);
        if (db == null)
            return null;

        Cursor cursor = persistableResource.getCursor(db);
        try {
            if (!cursor.moveToFirst())
                return null;

            List<E> cached = new ArrayList<>();
            do
                cached.add(persistableResource.loadFrom(cursor));
            while (cursor.moveToNext());
            return cached;
        } finally {
            cursor.close();
        }
    }
}
