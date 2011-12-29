package com.github.mobile.android.repo;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

public class RepoSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.github.search.suggest.recent.repos";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public static void saveRecentRepoQuery(Context context, String query) {
        suggestions(context).saveRecentQuery(query, null);
    }

    public static void clearRepoQueryHistory(Context context) {
        suggestions(context).clearHistory();
    }

    private static SearchRecentSuggestions suggestions(Context context) {
        return new SearchRecentSuggestions(context, AUTHORITY, MODE);
    }


    public RepoSearchRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}