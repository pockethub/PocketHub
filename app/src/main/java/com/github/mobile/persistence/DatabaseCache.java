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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    private <E> List<E> requestAndStore(SQLiteOpenHelper helper,
            PersistableResource<E> persistableResource) throws IOException {
        List<E> items = persistableResource.request();

        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                persistableResource.store(db, items);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return items;
    }

    private <E> List<E> loadFromDB(SQLiteOpenHelper helper,
            PersistableResource<E> persistableResource) {
        Cursor cursor = persistableResource.getCursor(helper
                .getReadableDatabase());
        try {
            if (!cursor.moveToFirst())
                return null;

            List<E> cached = new ArrayList<E>();
            do {
                cached.add(persistableResource.loadFrom(cursor));
            } while (cursor.moveToNext());
            return cached;
        } finally {
            cursor.close();
        }
    }

}
