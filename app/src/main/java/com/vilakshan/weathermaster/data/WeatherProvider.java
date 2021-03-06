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
package com.vilakshan.weathermaster.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

public class WeatherProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
    private static final SQLiteQueryBuilder sLocationQueryBuilder;
    private static final SQLiteQueryBuilder sWeatherQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sLocationQueryBuilder = new SQLiteQueryBuilder();
        sWeatherQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
        sLocationQueryBuilder.setTables(WeatherContract.LocationEntry.TABLE_NAME);
        sWeatherQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOC_SETTING + " = ?";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOC_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOC_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";


    private Cursor getWeather(
            Uri uri, String[] projection, String sortOrder) {

        return sWeatherQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getLocation(
            Uri uri, String[] projection, String sortOrder) {

        return sLocationQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatch = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatch.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER,
                WEATHER);
        uriMatch.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*",
                WEATHER_WITH_LOCATION);
        uriMatch.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*/#",
                WEATHER_WITH_LOCATION_AND_DATE);
        uriMatch.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION,
                LOCATION);
        return uriMatch;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = getWeather(uri, projection, sortOrder);
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = getLocation(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        try {
            switch (match) {
                case WEATHER: {
                    normalizeDate(values);
                    long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case LOCATION: {
                    long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return returnUri;
        }


    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int affectedRows = 0;
        try {
            switch (match) {
                case WEATHER: {
                    int rows = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection,
                            selectionArgs);
                    if (rows > 0)
                        affectedRows = rows;
                    else
                        throw new android.database.SQLException("Failed to delete row from " + uri);
                    break;
                }
                case LOCATION: {
                    int rows = db.delete(WeatherContract.LocationEntry.TABLE_NAME, selection,
                            selectionArgs);
                    if (rows > 0)
                        affectedRows = rows;
                    else
                        throw new android.database.SQLException("Failed to delete row from " + uri);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return affectedRows;
        }
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE,
                    WeatherContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int affectedRows = 0;
        try {
            switch (match) {
                case WEATHER: {
                    int rows = db.update(WeatherContract.WeatherEntry.TABLE_NAME, values,
                            selection, selectionArgs);
                    if (rows > 0)
                        affectedRows = rows;
                    else
                        throw new android.database.SQLException("Failed to delete row from " + uri);
                    break;
                }
                case LOCATION: {
                    int rows = db.update(WeatherContract.LocationEntry.TABLE_NAME, values,
                            selection, selectionArgs);
                    if (rows > 0)
                        affectedRows = rows;
                    else
                        throw new android.database.SQLException("Failed to delete row from " + uri);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return affectedRows;
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
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

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}