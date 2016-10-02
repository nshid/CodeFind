package com.bazz_techtronics.codefind;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;

import com.bazz_techtronics.codefind.sync.CodeFindSyncAdapter;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TOPICFRAGMENT_TAG = "TFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mThreePane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.code_topic_container) != null &&
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
                        .replace(R.id.code_topic_container, new TopicFragment(), TOPICFRAGMENT_TAG)
                        .commit();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.code_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mThreePane = false;
        }

        SearchFragment searchFragment =  ((SearchFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_search));
        searchFragment.setUseTodayLayout(!mThreePane);

        CodeFindSyncAdapter.initializeSyncAdapter(this);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Searching source code repositories...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }
}