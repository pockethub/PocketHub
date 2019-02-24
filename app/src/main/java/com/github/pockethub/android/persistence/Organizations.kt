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

import android.content.Context
import com.github.pockethub.android.Database
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.organizations.OrganizationService
import com.meisolsson.githubsdk.service.users.UserService
import java.io.IOException
import java.util.ArrayList
import javax.inject.Inject

/**
 * Cache of organization under an account
 */
class Organizations
/**
 * Create organizations cache with services to load from
 *
 * @param context
 */
@Inject
constructor(private val context: Context) : PersistableResource<User> {

    private val allOrgs: MutableList<User>
        get() {
            val repos = ArrayList<User>()
            var current = 1
            var last = -1

            while (current != last) {
                val page = ServiceGenerator.createService(context, OrganizationService::class.java)
                    .getMyOrganizations(current.toLong())
                    .blockingGet()
                    .body()

                repos.addAll(page!!.items())
                last = if (page.last() != null) page.last()!! else -1
                current = if (page.next() != null) page.next()!! else -1
            }

            return repos
        }

    override fun loadItems(database: Database): List<User> {
        return database.organizationsQueries
            .selectUserAndOrgs { id, login, name, avatarUrl -> User.builder().id(id).login(login).name(name).avatarUrl(avatarUrl).build() }
            .executeAsList()
    }


    override fun store(db: Database, orgs: List<User>) {
        db.organizationsQueries.clearOrgs()
        if (orgs.isEmpty()) {
            return
        }

        for (user in orgs) {
            db.organizationsQueries.insertOrg(user.id())

            db.organizationsQueries.replaceUser(
                user.id(),
                user.login(),
                user.name(),
                user.avatarUrl()
            )
        }
    }

    @Throws(IOException::class)
    override fun request(): List<User> {
        val user = ServiceGenerator.createService(context, UserService::class.java).user
            .blockingGet()
            .body()

        val all = allOrgs
        all.add(user!!)
        return all
    }
}
