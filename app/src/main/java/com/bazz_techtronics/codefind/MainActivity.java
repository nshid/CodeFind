package com.bazz_techtronics.codefind;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SEARCHFRAGMENT_TAG = "SFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mThreePane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.code_search_container) != null &&
                findViewById(R.id.code_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in three-pane mode.
            mThreePane = true;
            // In three-pane mode, show the topic and detail view in this activity by
            // adding or replacing the respective fragments using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.code_search_container, new SearchResultsFragment(), SEARCHFRAGMENT_TAG)
                        .commit();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.code_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mThreePane = false;
        }

        SearchFragment searchFragment = ((SearchFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_search));
        searchFragment.setUseNormalLayout(!mThreePane);

        /*** REMOVE COMMENTS AFTER CONFIGURATION ***/
        //CodeFindSyncAdapter.initializeSyncAdapter(this);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchView searchView = (SearchView) findViewById(R.id.searchview_search);
                String searchQuery = searchView.getQuery().toString().trim();
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    Snackbar.make(view, "Searching source code repositories...for " + searchQuery, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, searchQuery);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_minimal, menu);

        return true;
    }
}