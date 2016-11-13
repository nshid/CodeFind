package com.bazz_techtronics.codefind;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bazz_techtronics.codefind.sync.FetchResponseTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Encapsulates fetching the code topics and displaying it as a {@link ListView} layout.
 */
public class SearchResultsFragment extends Fragment {

    public static final String LOG_TAG = SearchResultsFragment.class.getSimpleName();

    private String mSearchStr;
    private ArrayAdapter<String> mSearchAdapter;

    private ArrayList mSearchScores = new ArrayList<String>();
    private ArrayList mSearchQuestions = new ArrayList<String>();
    private ArrayList<ArrayList<String>> mSearchTags = new ArrayList<>();
    private ArrayList<ArrayList<String>> mSearchAnswers = new ArrayList<>();
    private ArrayList<ArrayList<String>> mSearchComments = new ArrayList<>();

    String[] values = new String[] { "No results found!" };

    public SearchResultsFragment() {
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

        Intent intent = getActivity().getIntent();
        if (mSearchStr != null && !mSearchStr.isEmpty()) {
            SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            searchView.setQuery(mSearchStr, false);
            searchView.setIconified(false);
            searchView.clearFocus();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.onActionViewCollapsed();
                    if (query != null && !query.trim().isEmpty()) {
                        searchView.setQuery(query, false);
                        searchView.setIconified(false);
                    }
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mSearchStr = newText;
                    mSearchAdapter.notifyDataSetChanged();
                    return true;
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_topic, container, false);

        // One the root view for the Fragment has been created, it's time to create
        // the ListView with some dummy data.

        ArrayList<String> editableList = new ArrayList<String>(Arrays.asList(values));
        mSearchAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, editableList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (null == convertView) {
                    row = inflater.inflate(android.R.layout.simple_list_item_1, null);
                } else {
                    row = convertView;
                }

                TextView tv = (TextView) row.findViewById(android.R.id.text1);
                if (getItem(position) != null) {
                    tv.setTextColor(Color.WHITE);

                    String filter = mSearchStr;
                    String itemValue = Html.fromHtml(getItem(position)).toString();

                    int startPos = itemValue.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
                    int endPos = startPos + filter.length();

                    if (startPos != -1) // This should always be true, just a sanity check
                    {
                        Spannable spannable = new SpannableString(itemValue);
                        ColorStateList orangeColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(255, 140, 0)});
                        TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, orangeColor, null);

                        spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.setText(spannable);
                    } else {
                        tv.setText(itemValue);
                    }
                }

                return row;
            }

        };
        // Get a reference to the ListView, and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.listview_search);
        if (listView != null) {
            listView.setAdapter(mSearchAdapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (listView.getItemAtPosition(position) != null && mSearchQuestions.size() > 0) {
                    String selectedFromList = (listView.getItemAtPosition(position)).toString();
                    Toast.makeText(getContext(), Html.fromHtml(selectedFromList).toString(), Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, "Searching source code repositories...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    Intent detailIntent = new Intent(getContext(), DetailActivity.class);
                    detailIntent.putStringArrayListExtra("tags", mSearchTags.get(position));
                    detailIntent.putStringArrayListExtra("answers", mSearchAnswers.get(position));
                    detailIntent.putStringArrayListExtra("comments", mSearchComments.get(position));
                    detailIntent.putExtra("score", mSearchScores.get(position).toString());
                    detailIntent.putExtra("question_title", selectedFromList);
                    detailIntent.putExtra("question_body", mSearchQuestions.get(position).toString());
                    detailIntent.putExtra("query", mSearchStr);

                    startActivity(detailIntent);
                }
            }
        });

        // The search results Activity called via intent.  Inspect the intent for search data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mSearchStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            FetchResponseTask questionResponseTask = new FetchResponseTask(getActivity(), getContext(), mSearchAdapter, mSearchAnswers, mSearchComments, mSearchQuestions, mSearchScores, mSearchTags);
            questionResponseTask.execute(mSearchStr);
        }

        Snackbar.make(rootView, "Searching source code repositories...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        return rootView;
    }
}
