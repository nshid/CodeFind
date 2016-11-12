package com.bazz_techtronics.codefind;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bazz_techtronics.codefind.data.Cache;
import com.bazz_techtronics.codefind.data.InternalStorage;

import java.io.IOException;

/**
 * Created by Nshidbaby on 9/20/2016.
 */

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static boolean showQuestions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_enable_questions_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_questions_default)));
    }

    public static boolean showComments(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_enable_comments_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_comments_default)));
    }

    public static void insertCacheData(Context context, Cache cache, String key) {
        try {
            // Insert the cache object into internal storage
            InternalStorage.writeObject(context, key, cache);
        } catch (IOException e1) {
            Log.e(LOG_TAG, e1.getMessage());
        } catch (ClassCastException e2) {
            Log.e(LOG_TAG, e2.getMessage());
        }
    }

    public static Object retrieveCacheData(Context context, String key) {
        try {
            // Retrieve all entries from internal storage
            return InternalStorage.readObject(context, key);
        } catch (IOException e1) {
            Log.e(LOG_TAG, e1.getMessage());
        } catch (ClassNotFoundException e2) {
            Log.e(LOG_TAG, e2.getMessage());
        }
        return null;
    }

    public static void deleteCacheData(Context context, String key) {
        try {
            // Delete all entries from internal storage
            InternalStorage.deleteObject(context, key);
        } catch (IOException e1) {
            Log.e(LOG_TAG, e1.getMessage());
        } catch (ClassNotFoundException e2) {
            Log.e(LOG_TAG, e2.getMessage());
        }
    }

    public static void setSearchViewOnClickListener(Boolean focus, View v, View.OnClickListener listener) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)v;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout || child instanceof RelativeLayout) {
                    setSearchViewOnClickListener(focus, child, listener);
                }

                if (child instanceof TextView) {
                    TextView text = (TextView)child;
                    text.setFocusable(focus);
                }

                // do not set listener on clear button
                if (!(child instanceof ImageView))
                    child.setOnClickListener(listener);
            }
        }
    }
}