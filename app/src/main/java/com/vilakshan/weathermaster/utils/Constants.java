package com.vilakshan.weathermaster.utils;

import com.vilakshan.weathermaster.data.WeatherContract;

/**
 * <p>
 * Created by VilakshanSaxena on 12/19/2015.
 * </p>
 */
public class Constants {
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;
    public static final int COL_HUMIDITY = 9;
    public static final int COL_WIND_SPEED = 10;
    public static final int COL_PRESSURE = 11;
    public static final int COL_DEGREES = 12;
    public static final int COL_ICON_NAME = 13;
    public static final int COL_CITY_NAME = 14;

    public static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOC_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_ICON_NAME,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_LONG_DESC
    };

    public static final String SYNC_ACCOUNT_CREATED_KEY = "SYNC_ACCOUNT";

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public static final long HOURS_IN_MILLIS = 1000 * 60 * 60;
    public static final int WEATHER_NOTIFICATION_ID = 3004;
    public static final int NOTIFICATION_DAY_THRESHOLD = 8;
    public static final int NOTIFICATION_NIGHT_THRESHOLD = 20;

    public static final String MAIN_ICON_TRANSITION_STRING = "TN_Icon";
    public static final int GPS_DURATION = 25000;
    public static final String SPLASH_SCREEN_FLAG = "SC";
    public static final String SPLASH_SCREEN_FLAG_VALUE = "SC_VAL";

    public static int SPLASH_TIME_OUT = 2000;
    public static int TEXT_COLOR_CHANGE_DURATION = 1000;
    public static final int RED = 0xffFF8080;
    public static final int BLUE = 0xff8080FF;

    public static final String DEFAULT_API_KEY = "1";
    public static final String API_KEY_1 = "1";
    public static final String API_KEY_2 = "2";
    public static final String API_KEY_3 = "3";

    public static final int GPS_PERMISSION_CODE = 1;

    public static final String DOT =".";

    public static final String DAY_CONSTANT_KEY = "DAY";
    public static final String MORNING_CONSTANT = "M";
    public static final String NIGHT_CONSTANT = "N";
}
