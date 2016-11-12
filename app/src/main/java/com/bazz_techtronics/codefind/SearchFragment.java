package com.bazz_techtronics.codefind;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.bazz_techtronics.codefind.data.CodeContract;

/**
 * Encapsulates fetching the code topics and displaying it as a {@link SearchView} layout.
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = SearchFragment.class.getSimpleName();
    private ArrayAdapter<String> mSearchAdapter;

    private String mSearchStr;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseNormalLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int CODE_LOADER = 0;
    // For the code view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] CODE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the search & code tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the search set by the user, which is only in the Search table.
            // So the convenience is worth it.
            CodeContract.CodeEntry.TABLE_NAME + "." + CodeContract.CodeEntry._ID,
            CodeContract.CodeEntry.COLUMN_DATE,
            CodeContract.CodeEntry.COLUMN_SHORT_DESC,
            CodeContract.CodeEntry.COLUMN_MAX_TEMP,
            CodeContract.CodeEntry.COLUMN_MIN_TEMP,
            CodeContract.SearchEntry.COLUMN_SEARCH_SETTING,
            CodeContract.CodeEntry.COLUMN_CODE_ID,
            CodeContract.SearchEntry.COLUMN_COORD_LAT,
            CodeContract.SearchEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to SEARCH_COLUMNS.  If CODE_COLUMNS changes, these
    // must change.
    static final int COL_CODE_ID = 0;
    static final int COL_CODE_DATE = 1;
    static final int COL_CODE_DESC = 2;
    static final int COL_CODE_MAX_TEMP = 3;
    static final int COL_CODE_MIN_TEMP = 4;
    static final int COL_SEARCH_SETTING = 5;
    static final int COL_CODE_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Topic/DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mSearchStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        SearchView searchView = (SearchView) rootView.findViewById(R.id.searchview_search);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused && searchView.getQuery().toString().trim().equals("")) {
                    searchView.onActionViewCollapsed();
                }
            }
        });
        Utility.setSearchViewOnClickListener(true, searchView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query != null && !query.trim().isEmpty()) {
                    Intent intent = new Intent(getContext(), SearchActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, query);

                    startActivity(intent);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CODE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the search when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
          /*************************************************/
         /* CHANGE AND UNCOMMENT AFTER DISPLAYING SCREENS */
        /*************************************************/
        //updateCode();
        //getLoaderManager().restartLoader(CODE_LOADER, null, this);
    }

    private void updateCode() {
        // sync immediately if not a duplicate listview item
//        if ( false == mSearchAdapter.getCursor().moveToFirst() )
//        {
//            /*** ENABLE SYNC AFTER CONFIGURATION ***/
//            //CodeFindSyncAdapter.syncImmediately(getActivity());
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = CodeContract.CodeEntry.COLUMN_DATE + " ASC";

//        String locationSetting = Utility.getPreferredLocation(getActivity());
        String searchSetting = "77040";
        Uri codeForSearchUri = CodeContract.CodeEntry.buildCodeSearchWithStartDate(
                searchSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                codeForSearchUri,
                CODE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /*** REMOVE COMMENTS AFTER CONFIGURATION ***/
//        mSearchAdapter.swapCursor(data);
//        if (mPosition != ListView.INVALID_POSITION) {
//            // If we don't need to restart the loader, and there's a desired position to restore
//            // to, do so now.
//            mListView.smoothScrollToPosition(mPosition);
//        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mSearchAdapter.swapCursor(null);
    }

    public void setUseNormalLayout(boolean useNormalLayout) {
        mUseNormalLayout = useNormalLayout;
//        if (mSearchAdapter != null) {
//            mSearchAdapter.setUseNormalLayout(mUseNormalLayout);
//        }
    }
}
