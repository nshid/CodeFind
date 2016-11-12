package com.bazz_techtronics.codefind;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bazz_techtronics.codefind.data.SquaredBackgroundSpan;
import com.bazz_techtronics.codefind.data.Tuple;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Encapsulates fetching the code topics and displaying it as a {@link ListView} layout.
 */
public class DetailFragment extends Fragment {

    public static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String SEARCH_SHARE_HASHTAG = " #CodeFindApp";

    private ShareActionProvider mShareActionProvider;
    private ArrayList<Integer> mSearchSpanIndices;
    private ArrayList mSearchComments;
    private ArrayList mSearchAnswers;
    private ArrayList mSearchTags;
    private String mSearchQuestions;
    private String mSearchScore;
    private String mSearchTitle;
    private String mSearchStr;

    private SharedPreferences.OnSharedPreferenceChangeListener settingsListener;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mSearchStr != null && !mSearchStr.trim().isEmpty()) {
            mShareActionProvider.setShareIntent(createShareSearchIntent());
        }
    }

    private Intent createShareSearchIntent() {
        StringBuilder builder = new StringBuilder();
        builder.append(mSearchTitle + "\n\n");
        builder.append(mSearchQuestions + "\n\n");
        if (mSearchAnswers != null && !mSearchAnswers.isEmpty())
            builder.append(TextUtils.join("\n", mSearchAnswers) + "\n\n");
        if (mSearchComments != null && !mSearchComments.isEmpty())
            builder.append(TextUtils.join("\n", mSearchComments));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mSearchStr + SEARCH_SHARE_HASHTAG);
        shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(builder.toString()));

        return shareIntent;
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
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // One the root view for the Fragment has been created, it's time to create
        // the ListView with some dummy data.

        settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                if (getActivity() != null)
                    getActivity().recreate();
            }
        };

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(settingsListener);

        // The detail Activity called via intent.  Inspect the intent for search data.
        Intent intent = getActivity().getIntent();

        if (intent.hasExtra("query"))
            mSearchStr = intent.getStringExtra("query");
        if (intent.hasExtra("question_title"))
            mSearchTitle = intent.getStringExtra("question_title");
        if (intent.hasExtra("question_body"))
            mSearchQuestions = intent.getStringExtra("question_body");
        if (intent.hasExtra("answers"))
            mSearchAnswers = intent.getStringArrayListExtra("answers");
        if (intent.hasExtra("comments"))
            mSearchComments = intent.getStringArrayListExtra("comments");
        if (intent.hasExtra("score")) {
            mSearchScore = intent.getStringExtra("score");
            int scoreVal = Integer.parseInt(mSearchScore);
            if (scoreVal > 0) {
                mSearchScore = "+" + mSearchScore;
            }
        }
        if (intent.hasExtra("tags"))
            mSearchTags = intent.getStringArrayListExtra("tags");

        TextView queryTextView = (TextView) rootView.findViewById(R.id.textview_query);
        if (queryTextView != null) {
            queryTextView.setTextColor(Color.WHITE);
            queryTextView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

            String filter = mSearchStr;
            String itemValue = Html.fromHtml(mSearchTitle).toString();

            int startPos = itemValue.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
            int endPos = startPos + filter.length();

            if (startPos != -1) // This should always be true, just a sanity check
            {
                Spannable spannable = new SpannableString(itemValue);
                ColorStateList redColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.RED});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, redColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                queryTextView.setText(spannable);
            } else {
                queryTextView.setText(itemValue);
            }
        }

        TextView scoreTextView = (TextView) rootView.findViewById(R.id.textview_score);
        if (scoreTextView != null) {
            scoreTextView.setTextColor(Color.WHITE);
            scoreTextView.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            scoreTextView.setBackgroundColor(Color.rgb(30, 144, 255)); // light-blue
            scoreTextView.setText(mSearchScore);
        }

        TextView tagTextView = (TextView) rootView.findViewById(R.id.textview_tags);
        if (tagTextView != null) {
            tagTextView.setTextColor(Color.WHITE);
            tagTextView.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            tagTextView.setText(TextUtils.join(", ", mSearchTags));
        }

        TextView textView = (TextView) rootView.findViewById(R.id.textview_detail);
        textView.setMovementMethod(LinkMovementMethod.getInstance()); // allow clickable links
        if (textView != null) {
            mSearchSpanIndices = new ArrayList<>();
            SpannableStringBuilder responseBuilder = new SpannableStringBuilder();

            appendSpannableHtml(getContext(), "QUESTIONS", "ALIGN_CENTER", Color.GREEN, new Tuple<>(true, Color.BLACK), new Tuple<>(true, Color.rgb(102,255,102)), responseBuilder);
            if (Utility.showQuestions(getContext())) {
                responseBuilder.append(Html.fromHtml("<div>" + mSearchQuestions + "</div>"));
            } else {
                appendSpannableHtml(getContext(), "Questions are hidden.", "ALIGN_CENTER", Color.DKGRAY, null, null, responseBuilder);

            }

            appendSpannableHtml(getContext(), "ANSWERS", "ALIGN_CENTER", Color.RED, new Tuple<>(true, Color.BLACK), new Tuple<>(true, Color.rgb(255,102,102)), responseBuilder);
            if (mSearchAnswers != null && !mSearchAnswers.isEmpty()) {
                appendToggleSpannableHtml(Color.rgb(255, 230, 230), Color.rgb(230, 230, 255), mSearchAnswers, responseBuilder);
            } else {
                appendSpannableHtml(getContext(), "No answers found.", "ALIGN_CENTER", Color.DKGRAY, null, null, responseBuilder);
            }

            appendSpannableHtml(getContext(), "COMMENTS", "ALIGN_CENTER", Color.YELLOW, new Tuple<>(true, Color.BLACK), new Tuple<>(true, Color.rgb(255,255,102)), responseBuilder);
            if (Utility.showComments(getContext())) {
                if (mSearchComments != null && !mSearchComments.isEmpty()) {
                    appendToggleSpannableHtml(Color.rgb(242, 242, 242), Color.rgb(235, 246, 249), mSearchComments, responseBuilder);
                } else {
                    appendSpannableHtml(getContext(), "No comments found.", "ALIGN_CENTER", Color.DKGRAY, null, null, responseBuilder);
                }
            } else {
                appendSpannableHtml(getContext(), "Comments are hidden.", "ALIGN_CENTER", Color.DKGRAY, null, null, responseBuilder);
            }
            textView.setText(responseBuilder);
        }
        return rootView;
    }

    public void appendSpannable(Context context, String filter, String alignment, Integer txtColor, Tuple bkgColor, Tuple bdrColor, SpannableStringBuilder strBuilder) {
        strBuilder.append(filter);
        setSpannable(context, filter, alignment, txtColor, bkgColor, bdrColor, strBuilder);
        if (strBuilder.getSpanStart(filter) > -1)
            strBuilder.replace(strBuilder.getSpanStart(filter), strBuilder.getSpanEnd(filter), Html.fromHtml(filter));
    }

    public void appendSpannableHtml(Context context, String filter, String alignment, Integer txtColor, Tuple bkgColor, Tuple bdrColor, SpannableStringBuilder strBuilder) {
        strBuilder.append(Html.fromHtml("<div>" + filter + "</div>"));
        setSpannable(context, filter, alignment, txtColor, bkgColor, bdrColor, strBuilder);
    }

    public void setSpannable(Context context, String filter, String alignment, Integer txtColor, Tuple bkgColor, Tuple bdrColor, SpannableStringBuilder strBuilder) {
        Integer startPos = getStartPos(filter, strBuilder);
        strBuilder.setSpan(new SquaredBackgroundSpan(context, alignment, txtColor, bkgColor, bdrColor), startPos, startPos + filter.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSearchSpanIndices.add(startPos + filter.length());
    }

    public void appendToggleSpannableHtml(Integer hiOnColor, Integer hiOffColor, ArrayList searchListEntries, SpannableStringBuilder strBuilder) {
        int toggle = 1;
        strBuilder.append(Html.fromHtml("<div>"));
        for (Object searchListEntry : searchListEntries) {
            Spannable highlight = (Spannable) Html.fromHtml("<div>" + searchListEntry.toString() + "</div>");
            int startPos = 0;
            int endPos = Html.fromHtml(searchListEntry.toString()).length();
            if (toggle == 1) {
                highlight.setSpan(new BackgroundColorSpan(hiOnColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                highlight.setSpan(new BackgroundColorSpan(hiOffColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            toggle = toggle * -1;
            strBuilder.append(highlight);
        }
        strBuilder.append(Html.fromHtml("</div>"));
    }

    public Integer getStartPos(String filter, SpannableStringBuilder strBuilder) {
        return strBuilder.toString().lastIndexOf(filter);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(settingsListener);
    }
}