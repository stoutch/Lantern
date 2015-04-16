package com.example.deept_000.masproject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Chris on 4/9/2015.
 */
public class SearchActivity extends Activity {
    ArrayAdapter<String> adapter;
    private final String PREFS_NAME = "suggestions";
    private final String KEY_PREFIX = "search_suggestion";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        ArrayList<String> places = getRecentPlaces();//{"Tech Tower", "750 Ferst Dr", "190 5th St NW Atlanta, GA", "Bobby dodd stadium"};
        ListView lv = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.product_name, places);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), InitialMapActivity.class);
                intent.putExtra(SearchManager.QUERY, item);
                intent.putExtra("new_search", false);
                intent.setAction(Intent.ACTION_SEARCH);
                startActivity(intent);
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchActivity.this,
//                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
//                suggestions.saveRecentQuery(item, null);
            }
        });
        //setupSearchView();
        setupSearchViewBasic();
    }

    private ArrayList<String> getRecentPlaces() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        int index = settings.getInt("recent_index", 0);
        ArrayList<String> results = new ArrayList<String>(index + 1);
        String place = "";
        boolean empty = true;
        for (int i = 0; i < index; i++) {
            place = settings.getString(KEY_PREFIX + i, "");
            if (!place.equals("")) {
                results.add(place);
                empty = false;
            }
        }
        return results;
    }

    private void setupSearchViewBasic() {
        LinearLayout searchContainer = (LinearLayout) findViewById(R.id.search_container);
        final EditText toolbarSearchView = (EditText) findViewById(R.id.search_view);
        ImageView searchClearButton = (ImageView) findViewById(R.id.search_clear);
        // Setup search container view
        try {
            // Set cursor colour to white
            // http://stackoverflow.com/a/26544231/1692770
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchActivity.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        toolbarSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = toolbarSearchView.getText().toString();
                    Intent intent = new Intent(getApplicationContext(), InitialMapActivity.class);
                    intent.putExtra(SearchManager.QUERY, text);
                    intent.putExtra("new_search", true);
                    intent.setAction(Intent.ACTION_SEARCH);
                    startActivity(intent);
                    handled = true;
                }
                return handled;
            }
        });

        // Clear search text when clear button is tapped
        searchClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarSearchView.setText("");
            }
        });
    }

    private void setupSearchView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Setup search container view
        LinearLayout searchContainer = new LinearLayout(this);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        searchContainer.setLayoutParams(containerParams);

        // Setup the back button
        ImageView backButton = new ImageView(this);
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backParams.gravity = Gravity.CENTER;
        backButton.setLayoutParams(backParams);
        backButton.setImageResource(R.drawable.ic_arrow_back_white_24dp); // TODO: Get this image from here: https://github.com/google/material-design-icons
        backButton.setPadding(px, 0, px, 0);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        ((LinearLayout) searchContainer).addView(backButton);

        // Setup search view
        final EditText toolbarSearchView = new EditText(this);
        // Set width / height / gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1;
        toolbarSearchView.setLayoutParams(params);

        // Setup display
        toolbarSearchView.setBackgroundColor(Color.TRANSPARENT);
        toolbarSearchView.setPadding(2, 0, 0, 0);
        toolbarSearchView.setTextColor(Color.WHITE);
        toolbarSearchView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarSearchView.setSingleLine(true);
        toolbarSearchView.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
        toolbarSearchView.setHint("Search");
        toolbarSearchView.setHintTextColor(Color.parseColor("#b3ffffff"));
        try {
            // Set cursor colour to white
            // http://stackoverflow.com/a/26544231/1692770
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor);
        } catch (Exception ignored) {
        }

        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Fragment mainFragment = getFragmentManager().findFragmentById(R.id.container);
//                if (mainFragment != null && mainFragment instanceof MainListFragment) {
//                    ((MainListFragment) mainFragment).search(s.toString());
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // http://stackoverflow.com/a/6438918/1692770
                if (s.toString().length() <= 0) {
                    toolbarSearchView.setHintTextColor(Color.parseColor("#b3ffffff"));
                }
            }
        });
        ((LinearLayout) searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        ImageView searchClearButton = new ImageView(this);
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        searchClearButton.setLayoutParams(clearParams);
        searchClearButton.setImageResource(R.drawable.ic_clear_white_24dp); // TODO: Get this image from here: https://github.com/google/material-design-icons
        searchClearButton.setPadding(px, 0, px, 0);
        searchClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarSearchView.setText("");
            }
        });
        ((LinearLayout) searchContainer).addView(searchClearButton);

        // Add search view to toolbar and hide it
        searchContainer.setVisibility(View.VISIBLE);
        toolbar.addView(searchContainer);
    }
}
