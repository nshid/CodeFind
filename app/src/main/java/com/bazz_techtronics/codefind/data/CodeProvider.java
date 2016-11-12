/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazz_techtronics.codefind.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class CodeProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CodeDbHelper mOpenHelper;

    static final int CODE = 100;
    static final int CODE_WITH_SEARCH = 101;
    static final int CODE_WITH_SEARCH_AND_DATE = 102;
    static final int SEARCH = 300;

    private static final SQLiteQueryBuilder sCodeBySearchSettingQueryBuilder;

    static{
        sCodeBySearchSettingQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //weather INNER JOIN SEARCH ON weather.SEARCH_id = SEARCH._id
        sCodeBySearchSettingQueryBuilder.setTables(
                CodeContract.CodeEntry.TABLE_NAME + " INNER JOIN " +
                        CodeContract.SearchEntry.TABLE_NAME +
                        " ON " + CodeContract.CodeEntry.TABLE_NAME +
                        "." + CodeContract.CodeEntry.COLUMN_SRC_KEY +
                        " = " + CodeContract.SearchEntry.TABLE_NAME +
                        "." + CodeContract.SearchEntry._ID);
    }

    //SEARCH.SEARCH_setting = ?
    private static final String sSearchSettingSelection =
            CodeContract.SearchEntry.TABLE_NAME+
                    "." + CodeContract.SearchEntry.COLUMN_SEARCH_SETTING + " = ? ";

    //SEARCH.SEARCH_setting = ? AND date >= ?
    private static final String sSearchSettingWithStartDateSelection =
            CodeContract.SearchEntry.TABLE_NAME+
                    "." + CodeContract.SearchEntry.COLUMN_SEARCH_SETTING + " = ? AND " +
                    CodeContract.CodeEntry.COLUMN_DATE + " >= ? ";

    //SEARCH.SEARCH_setting = ? AND date = ?
    private static final String sSearchSettingAndDaySelection =
            CodeContract.SearchEntry.TABLE_NAME +
                    "." + CodeContract.SearchEntry.COLUMN_SEARCH_SETTING + " = ? AND " +
                    CodeContract.CodeEntry.COLUMN_DATE + " = ? ";

    private Cursor getCodeBySearchSetting(Uri uri, String[] projection, String sortOrder) {
        String searchSetting = CodeContract.CodeEntry.getSearchSettingFromUri(uri);
        long startDate = CodeContract.CodeEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sSearchSettingSelection;
            selectionArgs = new String[]{searchSetting};
        } else {
            selectionArgs = new String[]{searchSetting, Long.toString(startDate)};
            selection = sSearchSettingWithStartDateSelection;
        }

        return sCodeBySearchSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCodeBySearchSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String searchSetting = CodeContract.CodeEntry.getSearchSettingFromUri(uri);
        long date = CodeContract.CodeEntry.getDateFromUri(uri);

        return sCodeBySearchSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sSearchSettingAndDaySelection,
                new String[]{searchSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, CODE_WITH_SEARCH, CODE_WITH_SEARCH_AND_DATE,
        and SEARCH integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CodeContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CodeContract.PATH_CODE, CODE);
        matcher.addURI(authority, CodeContract.PATH_CODE + "/*", CODE_WITH_SEARCH);
        matcher.addURI(authority, CodeContract.PATH_CODE + "/*/#", CODE_WITH_SEARCH_AND_DATE);

        matcher.addURI(authority, CodeContract.PATH_SEARCH, SEARCH);
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new CodeDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case CODE_WITH_SEARCH_AND_DATE:
                return CodeContract.CodeEntry.CONTENT_ITEM_TYPE;
            case CODE_WITH_SEARCH:
                return CodeContract.CodeEntry.CONTENT_TYPE;
            case CODE:
                return CodeContract.CodeEntry.CONTENT_TYPE;
            case SEARCH:
                return CodeContract.SearchEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "code/*/*"
            case CODE_WITH_SEARCH_AND_DATE:
            {
                retCursor = getCodeBySearchSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "code/*"
            case CODE_WITH_SEARCH: {
                retCursor = sCodeBySearchSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "code"
            case CODE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CodeContract.CodeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "SEARCH"
            case SEARCH: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CodeContract.SearchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert SEARCHs to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE: {
                normalizeDate(values);
                long _id = db.insert(CodeContract.CodeEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = CodeContract.CodeEntry.buildCodeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SEARCH: {
                    normalizeDate(values);
                    long _id = db.insert(CodeContract.SearchEntry.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = CodeContract.SearchEntry.buildSearchUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CODE:
                rowsDeleted = db.delete(
                        CodeContract.CodeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SEARCH:
                rowsDeleted = db.delete(
                        CodeContract.SearchEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(CodeContract.CodeEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(CodeContract.CodeEntry.COLUMN_DATE);
            values.put(CodeContract.CodeEntry.COLUMN_DATE, CodeContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
        Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case CODE:
                rowsUpdated = db.update(
                        CodeContract.CodeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SEARCH:
                rowsUpdated = db.update(
                        CodeContract.SearchEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(CodeContract.CodeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(14)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}