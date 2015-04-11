package com.example.deept_000.masproject;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Chris on 4/9/2015.
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.deept_000.masproject.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
