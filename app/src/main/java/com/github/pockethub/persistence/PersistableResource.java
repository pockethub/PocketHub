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

import java.io.IOException;
import java.util.List;

/**
 * Describes how to store, load or request-an-update-for a particular set of
 * data.
 *
 * @param <E>
 *            type of item
 */
public interface PersistableResource<E> {

    /**
     * @param readableDatabase
     * @return a cursor capable of reading the required information out of the
     *         database.
     */
    Cursor getCursor(SQLiteDatabase readableDatabase);

    /**
     * @param cursor
     * @return a single item, read from this row of the cursor
     */
    E loadFrom(Cursor cursor);

    /**
     * Store supplied items in DB, removing or updating prior entries
     *
     * @param writableDatabase
     * @param items
     */
    void store(SQLiteDatabase writableDatabase, List<E> items);

    /**
     * Request the data directly from the GitHub API, rather than attempting to
     * load it from the DB cache.
     *
     * @return list of items
     * @throws IOException
     */
    List<E> request() throws IOException;
}
