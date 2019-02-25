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
package com.github.pockethub.android.persistence

import android.util.Log
import com.github.pockethub.android.Database
import java.io.IOException
import javax.inject.Inject

/**
 * Given a PersistableResource, this class will take support loading/storing
 * it's data or requesting fresh data, as appropriate.
 */
open class DatabaseCache @Inject constructor() {

    @Inject
    protected lateinit var database: Database

    /**
     * Load or request given resources
     *
     * @param persistableResource
     * @return resource
     * @throws IOException
     */
    @Throws(IOException::class)
    fun <E> loadOrRequest(persistableResource: PersistableResource<E>): List<E> {
        val items = loadFromDB(persistableResource)
        if (items != null) {
            Log.d(TAG, "CACHE HIT: Found ${items.size} items for $persistableResource")
            return items
        }
        return requestAndStore(persistableResource)
    }

    /**
     * Request and store given resources
     *
     * @param persistableResource
     * @return resources
     * @throws IOException
     */
    fun <E> requestAndStore(persistableResource: PersistableResource<E>): List<E> {
        val items = persistableResource.request()
        database.transaction {
            persistableResource.store(database, items)
        }
        return items
    }

    private fun <E> loadFromDB(persistableResource: PersistableResource<E>): List<E>? {
        return persistableResource.loadItems(database)
    }

    companion object {

        private val TAG = "DatabaseCache"
    }
}
