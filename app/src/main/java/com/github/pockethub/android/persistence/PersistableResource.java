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

import com.github.pockethub.android.Database;

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
     * Load items from the database
     *
     * @param database
     * @return Item from the database
     */
    List<E> loadItems(Database database);

    /**
     * Store supplied items in DB, removing or updating prior entries
     *
     * @param database
     * @param items
     */
    void store(Database database, List<E> items);

    /**
     * Request the data directly from the GitHub API, rather than attempting to
     * load it from the DB cache.
     *
     * @return list of items
     * @throws IOException
     */
    List<E> request() throws IOException;
}
