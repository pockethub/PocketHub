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

import android.accounts.Account
import android.content.Context
import com.github.pockethub.android.Database
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Permissions
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.activity.WatchingService
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import io.reactivex.Single
import retrofit2.Response
import java.io.IOException
import java.util.ArrayList
import java.util.TreeSet
import javax.inject.Provider

/**
 * Cache of repositories under a given organization
 */

/**
 * Create repositories cache for a given organization
 *
 * @param orgs
 * @param context
 * @param accountProvider
 */
@AutoFactory
class OrganizationRepositories(
    private val org: User,
    @param:Provided private val context: Context,
    @param:Provided private val accountProvider: Provider<Account>
) : PersistableResource<Repository> {

    override fun loadItems(database: Database): List<Repository> {
        return database.repositoriesQueries.selectUserRepositories(
            org.id()
        ) { repoId, name, id, login, name_, avatarurl, private, fork, description, forks, watchers, language, hasIssues, mirrorUrl, permissions_admin, permissions_pull, permissions_push ->
            val owner = User.builder()
                .login(login)
                .id(id)
                .name(name_)
                .avatarUrl(avatarurl)
                .build()

            val permissions = Permissions.builder()
                .admin(permissions_admin)
                .push(permissions_push)
                .pull(permissions_pull)
                .build()

            Repository.builder()
                .owner(owner)
                .name(name)
                .id(repoId)
                .isPrivate(private)
                .isFork(fork)
                .description(description)
                .forksCount(forks)
                .watchersCount(watchers)
                .language(language)
                .hasIssues(hasIssues)
                .mirrorUrl(mirrorUrl)
                .permissions(permissions)
                .build()
        }.executeAsList()
    }
    private val isAuthenticatedUser: Boolean
        get() = org.login() == accountProvider.get().name

    override fun store(database: Database, repos: List<Repository>) {
        database.repositoriesQueries.clearUserRepositories(org.id())
        if (repos.isEmpty()) {
            return
        }

        for (repo in repos) {
            val owner = repo.owner()
            database.repositoriesQueries.insertRepo(
                repo.id(),
                repo.name(),
                org.id(),
                owner!!.id(),
                repo.isPrivate,
                repo.isFork,
                repo.description(),
                repo.forksCount(),
                repo.watchersCount(),
                repo.language(),
                repo.hasIssues(),
                repo.mirrorUrl(),
                repo.permissions()!!.admin(),
                repo.permissions()!!.pull(),
                repo.permissions()!!.push()
            )

            database.organizationsQueries.replaceUser(
                owner.id(),
                owner.name(),
                owner.login(),
                owner.avatarUrl()
            )
        }
    }

    @Throws(IOException::class)
    override fun request(): List<Repository> {
        if (isAuthenticatedUser) {
            val all = TreeSet<Repository> { repo1, repo2 ->
                val id1 = repo1.id()!!
                val id2 = repo2.id()!!
                if (id1 > id2) {
                    return@TreeSet 1
                }
                if (id1 < id2) {
                    return@TreeSet -1
                }
                0
            }

            all.addAll(getAllItems { page ->
                ServiceGenerator.createService(context, RepositoryService::class.java)
                    .getUserRepositories(page.toLong())
            })

            all.addAll(getAllItems { page ->
                ServiceGenerator.createService(context, WatchingService::class.java)
                    .getWatchedRepositories(page.toLong())
            })
            return ArrayList(all)
        } else {
            return getAllItems { page ->
                ServiceGenerator.createService(context, RepositoryService::class.java)
                    .getOrganizationRepositories(org.login(), page.toLong())
            }
        }
    }

    private fun getAllItems(request: (Int) -> Single<Response<Page<Repository>>>): List<Repository> {
        val repos = ArrayList<Repository>()
        var current = 1
        var last = -1

        while (current != last) {
            val page = request(current).blockingGet().body()
            repos.addAll(page!!.items())
            last = if (page.last() != null) page.last()!! else -1
            current = if (page.next() != null) page.next()!! else -1
        }

        return repos
    }

    override fun toString(): String {
        return javaClass.simpleName + '['.toString() + org.login() + ']'.toString()
    }
}
