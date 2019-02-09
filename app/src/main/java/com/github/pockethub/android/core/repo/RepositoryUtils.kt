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
package com.github.pockethub.android.core.repo

import com.meisolsson.githubsdk.model.Repository

/**
 * Utilities for working with [Repository] objects
 */
object RepositoryUtils {

    /**
     * Does the repository have details denoting it was loaded from an API call?
     *
     *
     * This uses a simple heuristic of either being private, being a fork, or
     * having a non-zero amount of forks or watchers, or has issues enable;
     * meaning it came back from an API call providing those details and more.
     *
     * @return true if complete, false otherwise
     */
    @JvmStatic
    fun isComplete(repository: Repository) =
        repository.isPrivate == true || repository.isFork == true || repository.hasIssues() == true
            || repository.forksCount() != null && repository.forksCount()!! > 0
            || repository.watchersCount() != null && repository.watchersCount()!! > 0

    /**
     * Is the given owner name valid?
     *
     * @param name
     * @return true if valid, false otherwise
     */
    @JvmStatic
    fun isValidOwner(name: String?) = name !in listOf(
            "about", "account", "admin", "api", "blog", "camo", "contact", "dashboard", "downloads",
            "edu", "explore", "features", "home", "inbox", "languages", "login", "logout", "new",
            "notifications", "organizations", "orgs", "repositories", "search", "security",
            "settings", "stars", "styleguide", "timeline", "training", "users", "watching")

    /**
     * Is the given repo name valid?
     *
     * @param name
     * @return true if valid, false otherwise
     */
    @JvmStatic
    fun isValidRepo(name: String?) = name !in listOf("followers", "following")
}
