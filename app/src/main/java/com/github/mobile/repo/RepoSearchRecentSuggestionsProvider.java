package com.github.mobile.repo;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

/**
 * Suggestions provider for recently searched for repository queries
 */
public class RepoSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    private final static String AUTHORITY = "com.github.search.suggest.recent.repos";

    private final static int MODE = DATABASE_MODE_QUERIES;

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