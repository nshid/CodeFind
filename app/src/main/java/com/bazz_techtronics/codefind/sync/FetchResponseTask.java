package com.bazz_techtronics.codefind.sync;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bazz_techtronics.codefind.Utility;
import com.bazz_techtronics.codefind.data.Cache;
import com.bazz_techtronics.codefind.data.Response;
import com.bazz_techtronics.codefind.data.Tuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nshidbaby on 11/7/2016.
 */

public class FetchResponseTask extends AsyncTask<String, Void, List<Response>> {

    private final String LOG_TAG = FetchResponseTask.class.getSimpleName();
    private final Integer MAX_RESULTS = 100; // max results to be returned
    private final Integer MAX_CACHE = 1024; // soft limit on cache entries

    private Cache qcache;

    private List searchTags;
    private List searchScores;
    private List searchAnswers;
    private List searchComments;
    private List searchQuestions;
    private String searchQueryURL;
    private ArrayAdapter searchAdapter;

    private Context searchContext;
    private Activity searchActivity;

    public FetchResponseTask(Activity searchActivity, Context searchContext, ArrayAdapter<String> searchAdapter, List searchAnswers, List searchComments, List searchQuestions, List searchScores, List searchTags) {

        this.searchContext = searchContext;
        this.searchActivity = searchActivity;
        this.searchAdapter = searchAdapter;
        this.searchTags = searchTags;
        this.searchScores = searchScores;
        this.searchAnswers = searchAnswers;
        this.searchComments = searchComments;
        this.searchQuestions = searchQuestions;

        this.searchQueryURL = "http://api.stackexchange.com/2.2/search?pagesize=" + MAX_RESULTS + "&order=desc&sort=activity&site=stackoverflow&filter=!3yXvh9)eIQCFlEPJH";

        this.qcache = (Cache) Utility.retrieveCacheData(searchContext, "R_CACHE");
        if (qcache == null) {
            this.qcache = new Cache();
        }
    }

    private List<Response> getSearchDataFromJson(String searchStr, String searchJsonStr, int numResults)
            throws JSONException {

        // JSON objects to be extracted
        final String STX_LIST = "items";
        final String STX_TAGS = "tags";
        final String STX_AID = "answer_id";
        final String STX_CID = "comment_id";
        final String STX_QID = "question_id";
        final String STX_TITLE = "title";
        final String STX_SCORE = "score";
        final String STX_BODY = "body";
        final String STX_LINK = "link";
        final String STX_ACNT = "answer_count";
        final String STX_CCNT = "comment_count";
        final String STX_ANSRS = "answers";
        final String STX_COMMS = "comments";

        JSONObject searchJson = new JSONObject(searchJsonStr);
        JSONArray searchArray = searchJson.getJSONArray(STX_LIST);

        ArrayList<Tuple<String, String>> questions = new ArrayList<>();

        List<Response> responseList = new ArrayList<>();
        for (int i = 0; i < searchArray.length() && i < numResults; i++) {
            Response response = new Response();

            // Get the JSON object representing the search query
            JSONObject searchObject = searchArray.getJSONObject(i);

            if (searchObject.has(STX_TAGS)) {
                // Get the JSON array representing the search tags
                JSONArray tagsArray = searchObject.getJSONArray(STX_TAGS);

                ArrayList<String> tagsList = new ArrayList<>();
                if (tagsArray != null) {
                    for (int t = 0; t < tagsArray.length(); t++) {
                        tagsList.add(tagsArray.get(t).toString());
                    }
                }
                response.setTags(tagsList);
            }

            if (searchObject.has(STX_COMMS)) {
                // Get the JSON array representing the search comments
                JSONArray commentsArray = searchObject.getJSONArray(STX_COMMS);

                ArrayList<Tuple<String, String>> commentsList = new ArrayList<>();
                if (commentsArray != null) {
                    for (int t = 0; t < commentsArray.length(); t++) {
                        JSONObject commentObject = commentsArray.getJSONObject(t);
                        commentsList.add(new Tuple<>(commentObject.getString(STX_CID), commentObject.has(STX_BODY) ? commentObject.getString(STX_BODY) : null));
                    }
                }
                response.setComments(commentsList);
            }
            if (searchObject.has(STX_CID)) {
                response.setCommentId(searchObject.getString(STX_CID));
                if (searchObject.has(STX_BODY))
                    response.setComment(searchObject.getString(STX_BODY));
            }
            response.setCommentCount(searchObject.getInt(STX_CCNT));

            if (searchObject.has(STX_QID)) {
                response.setQuestionId(searchObject.getString(STX_QID));
                response.setQuestionTitle(searchObject.getString(STX_TITLE));
                if (searchObject.has(STX_BODY))
                    response.setQuestion(searchObject.getString(STX_BODY));
            }

            if (searchObject.has(STX_ANSRS)) {
                // Get the JSON array representing the search answers
                JSONArray answersArray = searchObject.getJSONArray(STX_ANSRS);

                ArrayList<Tuple<String, String>> answersList = new ArrayList<>();
                if (answersArray != null) {
                    for (int t = 0; t < answersArray.length(); t++) {
                        JSONObject answerObject = answersArray.getJSONObject(t);
                        answersList.add(new Tuple<>(answerObject.getString(STX_AID), answerObject.has(STX_BODY) ? answerObject.getString(STX_BODY) : null));
                    }
                }
                response.setAnswers(answersList);
            }
            if (searchObject.has(STX_AID)) {
                response.setAnswerId(searchObject.getString(STX_AID));
                if (searchObject.has(STX_BODY))
                    response.setAnswer(searchObject.getString(STX_BODY));
            }
            response.setAnswerCount(searchObject.getInt(STX_ACNT));

            response.setScore(searchObject.getString(STX_SCORE));
            response.setLink(searchObject.getString(STX_LINK));

            responseList.add(response);
        }

        // Save the list of entries to internal storage
        if (!qcache.contains(searchStr) && (qcache.count() < MAX_CACHE)) {
            qcache.add(searchStr, responseList);
            Utility.insertCacheData(searchContext, qcache, "R_CACHE");
        }

        return responseList;
    }

    @Override
    protected List<Response> doInBackground(String... params) {

        // If there's no search string, there's nothing to look up. Verify size of params.
        if (params.length == 0 && params[0].trim().length() == 0) {
            return null;
        }

        String searchQueryStr = params[0].trim().replaceAll(" ", "+");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String searchJsonStr = null;

        try {
            // Ping cache first to avoid net lookup
            List<Response> cachedQuestions = qcache.getResults(searchQueryStr);
            if (cachedQuestions != null) {
                return cachedQuestions;
            }

            // Construct the URL for the StackExchange query
            // Possible parameters are available at StackExchange's search API page, at
            // https://api.stackexchange.com/docs/search
            final String SEARCH_BASE_URL = searchQueryURL;
            final String QUERY_PARAM = "intitle";
            final String APPID_PARAM = "key";

            Uri unbuiltUri = Uri.parse(SEARCH_BASE_URL);

            Uri builtUri = unbuiltUri.buildUpon().appendQueryParameter(QUERY_PARAM, searchQueryStr).build();
//                        .appendQueryParameter(APPID_PARAM, BuildConfig.STACK_APPS_API_KEY)
            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to StackExchange, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            searchJsonStr = buffer.toString();

            //Log.v(LOG_TAG, "Search JSON String: " + searchJsonStr);

            return getSearchDataFromJson(searchQueryStr, searchJsonStr, MAX_RESULTS);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        }  catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        // This will only happen if there was an error getting or parsing the search data.
        return null;
    }

    @Override
    protected void onPostExecute(List<Response> results) {
        if (results != null) {
            searchAdapter.clear();
                for (Response searchResponse : results) {
                    searchAdapter.add(searchResponse.getQuestionTitle());
                    searchComments.add(searchResponse.getComments());
                    searchAnswers.add(searchResponse.getAnswers());
                    searchQuestions.add(searchResponse.getQuestion());
                    searchScores.add(searchResponse.getScore());
                    searchTags.add(searchResponse.getTags());
                }
            searchActivity.runOnUiThread(new Runnable() {
                public void run() {
                    searchAdapter.notifyDataSetChanged();
                    Toast.makeText(searchContext, searchAdapter.getCount() + " results", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
