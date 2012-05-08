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
package com.github.mobile.ui.repo;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

/**
 * Suggestions provider for recently searched for repository queries
 */
public class RepoSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    private static final String AUTHORITY = "com.github.search.suggest.recent.repos";

    private static final int MODE = DATABASE_MODE_QUERIES;

    /**
     * Save query to history
     *
     * @param context
     * @param query
     */
    public static void saveRecentRepoQuery(Context context, String query) {
        suggestions(context).saveRecentQuery(query, null);
    }

    /**
     * Clear query history
     *
     * @param context
     */
    public static void clearRepoQueryHistory(Context context) {
        suggestions(context).clearHistory();
    }

    private static SearchRecentSuggestions suggestions(Context context) {
        return new SearchRecentSuggestions(context, AUTHORITY, MODE);
    }

    /**
     * Create suggestions provider for searched for repository queries
     */
    public RepoSearchRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}