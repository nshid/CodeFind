package com.bazz_techtronics.codefind;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nshidbaby on 9/20/2016.
 */

public class Utility {
    public static boolean showQuestions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_enable_comments_key),
                context.getString(R.string.pref_enable_questions_default))
                .equals(context.getString(R.string.pref_enable_questions_default));
    }

    public static boolean showComments(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_enable_comments_key),
                context.getString(R.string.pref_enable_comments_default))
                .equals(context.getString(R.string.pref_enable_comments_default));
    }
}
