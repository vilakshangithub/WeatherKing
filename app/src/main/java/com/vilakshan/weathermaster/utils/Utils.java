package com.vilakshan.weathermaster.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Time;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.google.gson.Gson;
import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.data.WeatherContract;
import com.vilakshan.weathermaster.entity.DayForecast;
import com.vilakshan.weathermaster.entity.Weather;
import com.vilakshan.weathermaster.entity.WeatherData;
import com.vilakshan.weathermaster.sync.WeatherMasterSyncAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/**
 * <p>
 * Created by Vilakshan Saxena on 10/27/2015.
 * </p>
 */

public class Utils {

    static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,
            LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID})
    public @interface LocationStatus {
    }

    private enum PermissionGroup {
        LOCATION {
            @Override
            public String toString() {
                return "LOCATION";
            }
        }
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(formatId, today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.ENGLISH);
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    private static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                     in Utils.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    private static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd", Locale.ENGLISH);
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utils.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static String getFormattedHumidity(Context context, double humidity) {
        return context.getString(R.string.format_humidity, humidity);
    }

    public static String getFormattedPressure(Context context, double pressure) {
        return context.getString(R.string.format_pressure, pressure);
    }


    public static Boolean getNotificationPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean result = prefs.getBoolean(context.getString(R.string.pref_key_notifications),
                Boolean.parseBoolean(context.getString(R.string.pref_default_notifications)));
        return result;
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_basic),
                context.getString(R.string.pref_default_value_basic));
    }

    public static String getTemperatureUnit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_basic_list),
                context.getString(R.string.pref_units_metric));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_basic_list),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    private static boolean isBetween(int id, int lower, int upper) {
        return lower <= id && id <= upper;
    }

    public static String getDescForWeatherCondition(Context context, int weatherId) {
        String weatherDesc = "";
        if (isBetween(weatherId, 200, 232)) {
            weatherDesc = context.getString(R.string.w_2xx);
        } else if (isBetween(weatherId, 300, 321)) {
            weatherDesc = context.getString(R.string.w_3xx);
        } else if (isBetween(weatherId, 500, 531)) {
            weatherDesc = context.getString(R.string.w_5xx);
        } else if (isBetween(weatherId, 600, 622)) {
            weatherDesc = context.getString(R.string.w_6xx);
        } else if (isBetween(weatherId, 701, 781)) {
            weatherDesc = context.getString(R.string.w_7xx);
        } else if (weatherId == 800) {
            weatherDesc = context.getString(R.string.w_800);
        } else if (isBetween(weatherId, 801, 804)) {
            weatherDesc = context.getString(R.string.w_80x);
        } else if (isBetween(weatherId, 900, 906)) {
            weatherDesc = context.getString(R.string.w_90x);
        } else if (isBetween(weatherId, 951, 962)) {
            weatherDesc = context.getString(R.string.w_9xx);
        } else {
            weatherDesc = "";
        }
        return weatherDesc;
    }

    public static String getDetailedDescForWeatherCondition(Context context, int weatherId) {
        String weatherDesc = "";
        switch (weatherId) {
            case 200:
                weatherDesc = context.getString(R.string.wd_200);
                break;
            case 201:
                weatherDesc = context.getString(R.string.wd_201);
                break;
            case 202:
                weatherDesc = context.getString(R.string.wd_202);
                break;
            case 210:
                weatherDesc = context.getString(R.string.wd_210);
                break;
            case 211:
                weatherDesc = context.getString(R.string.wd_211);
                break;
            case 212:
                weatherDesc = context.getString(R.string.wd_212);
                break;
            case 221:
                weatherDesc = context.getString(R.string.wd_221);
                break;
            case 230:
                weatherDesc = context.getString(R.string.wd_230);
                break;
            case 231:
                weatherDesc = context.getString(R.string.wd_231);
                break;
            case 232:
                weatherDesc = context.getString(R.string.wd_232);
                break;
            case 300:
                weatherDesc = context.getString(R.string.wd_300);
                break;
            case 301:
                weatherDesc = context.getString(R.string.wd_301);
                break;
            case 302:
                weatherDesc = context.getString(R.string.wd_302);
                break;
            case 310:
                weatherDesc = context.getString(R.string.wd_310);
                break;
            case 311:
                weatherDesc = context.getString(R.string.wd_311);
                break;
            case 312:
                weatherDesc = context.getString(R.string.wd_312);
                break;
            case 313:
                weatherDesc = context.getString(R.string.wd_313);
                break;
            case 314:
                weatherDesc = context.getString(R.string.wd_314);
                break;
            case 321:
                weatherDesc = context.getString(R.string.wd_321);
                break;
            case 500:
                weatherDesc = context.getString(R.string.wd_500);
                break;
            case 501:
                weatherDesc = context.getString(R.string.wd_501);
                break;
            case 502:
                weatherDesc = context.getString(R.string.wd_502);
                break;
            case 503:
                weatherDesc = context.getString(R.string.wd_503);
                break;
            case 504:
                weatherDesc = context.getString(R.string.wd_504);
                break;
            case 511:
                weatherDesc = context.getString(R.string.wd_511);
                break;
            case 520:
                weatherDesc = context.getString(R.string.wd_520);
                break;
            case 521:
                weatherDesc = context.getString(R.string.wd_521);
                break;
            case 522:
                weatherDesc = context.getString(R.string.wd_522);
                break;
            case 531:
                weatherDesc = context.getString(R.string.wd_531);
                break;
            case 600:
                weatherDesc = context.getString(R.string.wd_600);
                break;
            case 601:
                weatherDesc = context.getString(R.string.wd_601);
                break;
            case 602:
                weatherDesc = context.getString(R.string.wd_602);
                break;
            case 611:
                weatherDesc = context.getString(R.string.wd_611);
                break;
            case 612:
                weatherDesc = context.getString(R.string.wd_612);
                break;
            case 615:
                weatherDesc = context.getString(R.string.wd_615);
                break;
            case 616:
                weatherDesc = context.getString(R.string.wd_616);
                break;
            case 620:
                weatherDesc = context.getString(R.string.wd_620);
                break;
            case 621:
                weatherDesc = context.getString(R.string.wd_621);
                break;
            case 622:
                weatherDesc = context.getString(R.string.wd_622);
                break;
            case 701:
                weatherDesc = context.getString(R.string.wd_701);
                break;
            case 711:
                weatherDesc = context.getString(R.string.wd_711);
                break;
            case 731:
                weatherDesc = context.getString(R.string.wd_731);
                break;
            case 741:
                weatherDesc = context.getString(R.string.wd_741);
                break;
            case 751:
                weatherDesc = context.getString(R.string.wd_751);
                break;
            case 761:
                weatherDesc = context.getString(R.string.wd_761);
                break;
            case 762:
                weatherDesc = context.getString(R.string.wd_762);
                break;
            case 771:
                weatherDesc = context.getString(R.string.wd_771);
                break;
            case 781:
                weatherDesc = context.getString(R.string.wd_781);
                break;
            case 800:
                weatherDesc = context.getString(R.string.wd_800);
                break;
            case 801:
                weatherDesc = context.getString(R.string.wd_801);
                break;
            case 802:
                weatherDesc = context.getString(R.string.wd_802);
                break;
            case 803:
                weatherDesc = context.getString(R.string.wd_803);
                break;
            case 804:
                weatherDesc = context.getString(R.string.wd_804);
                break;
            case 900:
                weatherDesc = context.getString(R.string.wd_900);
                break;
            case 901:
                weatherDesc = context.getString(R.string.wd_901);
                break;
            case 902:
                weatherDesc = context.getString(R.string.wd_902);
                break;
            case 903:
                weatherDesc = context.getString(R.string.wd_903);
                break;
            case 904:
                weatherDesc = context.getString(R.string.wd_904);
                break;
            case 905:
                weatherDesc = context.getString(R.string.wd_905);
                break;
            case 906:
                weatherDesc = context.getString(R.string.wd_906);
                break;
            case 951:
                weatherDesc = context.getString(R.string.wd_951);
                break;
            case 952:
                weatherDesc = context.getString(R.string.wd_952);
                break;
            case 953:
                weatherDesc = context.getString(R.string.wd_953);
                break;
            case 954:
                weatherDesc = context.getString(R.string.wd_954);
                break;
            case 955:
                weatherDesc = context.getString(R.string.wd_955);
                break;
            case 956:
                weatherDesc = context.getString(R.string.wd_956);
                break;
            case 957:
                weatherDesc = context.getString(R.string.wd_957);
                break;
            case 958:
                weatherDesc = context.getString(R.string.wd_958);
                break;
            case 959:
                weatherDesc = context.getString(R.string.wd_959);
                break;
            case 960:
                weatherDesc = context.getString(R.string.wd_960);
                break;
            case 961:
                weatherDesc = context.getString(R.string.wd_961);
                break;
            case 962:
                weatherDesc = context.getString(R.string.wd_962);
                break;
            default:
                weatherDesc = "";

        }
        return weatherDesc;
    }

    private static Uri getIconURLForWeatherCondition(String iconName) {
        final String ICON_BASE_URL =
                "http://openweathermap.org/img/w";
        Uri builtUri = Uri.parse(ICON_BASE_URL).buildUpon()
                .appendPath(iconName + ".png")
                .build();
        return builtUri;
    }

    /**
     * Checking Internet Connectivity
     */
    public static boolean IsConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        return (nInfo != null && nInfo.isConnected());
    }

    /**
     * Checking GPS Availability
     */
    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SuppressWarnings("ResourceType")
    public static
    @LocationStatus
    int getLocationStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.key_location_preference),
                LOCATION_STATUS_UNKNOWN);
    }

    private static void setLocationStatus(Context c, @LocationStatus int locationStatus) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putInt(c.getString(R.string.key_location_preference), locationStatus);
        prefsEdit.apply();
    }

    public static void resetLocationStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putInt(context.getString(R.string.key_location_preference), LOCATION_STATUS_UNKNOWN);
        prefsEdit.apply();
    }

    public static void setAPIKey(Context context, String apiKey) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(context.getString(R.string.key_owm_api), apiKey);
        prefsEdit.commit();
    }

    public static String getAPIKey(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey = prefs.getString(context.getString(R.string.key_owm_api), Constants.DEFAULT_API_KEY);
        return apiKey;
    }

    public static void setDayConstant(Context context, String dayConstant) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(Constants.DAY_CONSTANT_KEY, dayConstant);
        prefsEdit.commit();
    }

    public static String getDayConstant(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dayConstant = prefs.getString(Constants.DAY_CONSTANT_KEY, Constants.MORNING_CONSTANT);
        return dayConstant;
    }

    public static void loadImage(Context context, String iconName, Object target) {
        if (target instanceof ImageView) {
            Glide
                    .with(context.getApplicationContext())
                    .load(Utils.getIconURLForWeatherCondition(iconName))
                    .asBitmap()
                    .fitCenter()
                    .error(R.drawable.nowthr)
                    .into((ImageView) target);
        } else if (target instanceof AppWidgetTarget) {
            Glide
                    .with(context.getApplicationContext())
                    .load(Utils.getIconURLForWeatherCondition(iconName))
                    .asBitmap()
                    .fitCenter()
                    .error(R.drawable.nowthr)
                    .into((AppWidgetTarget) target);
        } else if (target instanceof NotificationTarget) {
            Glide
                    .with(context.getApplicationContext())
                    .load(Utils.getIconURLForWeatherCondition(iconName))
                    .asBitmap()
                    .fitCenter()
                    .error(R.drawable.nowthr)
                    .into((NotificationTarget) target);
        }
    }

    public static boolean hasMarshMallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void showToastMessage(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    private static void showDialogForMarshmallow(final Activity activity, String permissionText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(permissionText);
        builder.setCancelable(false);
        builder.setPositiveButton(activity.getString(R.string.marshmallowPositive),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int id) {
                        final Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        showSystemSettings(activity);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(activity.getString(R.string.marshmallowNegative),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static void showSystemSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static String showPermissionMessageForMarshMallow(final Activity activity, final String...
            permission) {
        String permissionText = "";
        PermissionInfo permissionInfo;
        try {
            permissionInfo = activity.getPackageManager().getPermissionInfo(permission[0],
                    PackageManager.GET_META_DATA);
            PermissionGroup permissionGroup = PermissionGroup.valueOf(
                    permissionInfo.group.substring(permissionInfo.group.lastIndexOf(Constants.DOT) + 1)
                            .toUpperCase());
            permissionText = permissionGroup.toString().toLowerCase();
            switch (permissionGroup) {
                case LOCATION:
                    permissionText = String.format(activity.getString(R.string.marshmallow_permission_msg), permissionText);
                    break;
            }
            showDialogForMarshmallow(activity, permissionText);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            permissionText = "";
        }
        return permissionText;
    }

    public static void setSyncAccountCreatedFlag(Context context, boolean flag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putBoolean(Constants.SYNC_ACCOUNT_CREATED_KEY, flag);
        prefsEdit.commit();
    }

    public static boolean getSyncAccountCreatedFlag(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean syncAccountFlag = prefs.getBoolean(Constants.SYNC_ACCOUNT_CREATED_KEY, false);
        return syncAccountFlag;
    }

    public static void saveWeatherDataFromResponse(Context context, String weatherResponse) {
        if (TextUtils.isEmpty(weatherResponse)) {
            setLocationStatus(context, LOCATION_STATUS_SERVER_DOWN);
        } else {
            try {
                WeatherData weatherData = new Gson().fromJson(weatherResponse, WeatherData.class);
                if (weatherData != null) {
                    switch (weatherData.responseCode) {
                        case HttpURLConnection.HTTP_OK:
                            saveWeatherData(context, weatherData);
                            break;
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            setLocationStatus(context, LOCATION_STATUS_INVALID);
                            break;
                        default:
                            setLocationStatus(context, LOCATION_STATUS_SERVER_DOWN);
                            break;
                    }
                }
            } catch (Exception ex) {
                setLocationStatus(context, LOCATION_STATUS_SERVER_INVALID);
                ex.printStackTrace();
            }
        }
    }

    private static void saveWeatherData(Context context, WeatherData weatherData) {
        boolean isDataEligibleForStorage = false;
        if (weatherData.city != null && weatherData.city.coordinate != null &&
                weatherData.dayForecastList != null && weatherData.dayForecastList.size() > 0) {
            isDataEligibleForStorage = true;
        }
        if (isDataEligibleForStorage) {
            String cityName = weatherData.city.name;
            double cityLatitude = weatherData.city.coordinate.latitude;
            double cityLongitude = weatherData.city.coordinate.longitude;
            ArrayList<DayForecast> dayForecastList = weatherData.dayForecastList;
            long locationId = addLocation(context, getPreferredLocation(context), cityName,
                    cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(dayForecastList.size());

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.
            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for (int i = 0; i < dayForecastList.size(); i++) {
                long dateTime;
                DayForecast dayForecast = dayForecastList.get(i);
                Weather weather = dayForecast.weatherList.get(0);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                        dayForecast.humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                        dayForecast.pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                        dayForecast.windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES,
                        dayForecast.windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        dayForecast.temperature.maxTemperature);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                        dayForecast.temperature.minTemperature);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        weather.weatherDescription);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LONG_DESC,
                        weather.weatherFullDescription);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                        weather.weatherId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ICON_NAME,
                        weather.weatherIcon);

                cVVector.add(weatherValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                context.getContentResolver().delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.COLUMN_LOC_KEY + "=?",
                        new String[]{String.valueOf(locationId)});
                context.getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        cvArray);

                /*When data successfully changed through network call then notify the App Widget
                 Provider through sending a broadcast*/
                Intent dataUpdatedIntent = new Intent(WeatherMasterSyncAdapter.ACTION_DATA_UPDATED);
                context.sendBroadcast(dataUpdatedIntent);
                setLocationStatus(context, LOCATION_STATUS_OK);
            }
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    private static long addLocation(Context context, String locationSetting, String cityName,
                                    double lat, double lon) {
        Cursor locationCursor = context.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI, null, null, null, null);
        boolean isInsertRequired;
        long locationRowId = -1;
        if (!CursorUtils.isCursorEmpty(locationCursor) && locationCursor.moveToFirst()) {
            isInsertRequired = false;
            do {
                if (locationSetting.toUpperCase().equals(locationCursor.getString(
                        locationCursor.getColumnIndex(
                                WeatherContract.LocationEntry.COLUMN_LOC_SETTING)).toUpperCase())) {
                    locationRowId = locationCursor.getLong(locationCursor.getColumnIndex(
                            WeatherContract.LocationEntry._ID));
                    break;
                }
            } while (locationCursor.moveToNext());

            if (locationRowId == -1) {
                isInsertRequired = true;
            }
        } else {
            isInsertRequired = true;
        }
        if (isInsertRequired) {
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOC_SETTING,
                    locationSetting.toUpperCase());
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            Uri locationUri = context.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            locationRowId = ContentUris.parseId(locationUri);

        }
        return locationRowId;
    }
}

