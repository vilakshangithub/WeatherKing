package com.vilakshan.weathermaster.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.data.WeatherContract;
import com.vilakshan.weathermaster.sync.WeatherMasterSyncAdapter;
import com.vilakshan.weathermaster.ui.MainActivity;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.CursorUtils;
import com.vilakshan.weathermaster.utils.Utils;

import java.util.Calendar;

import service.WidgetService;

/**
 * <p>
 * Created by Vilakshan Saxena on 11/14/2015.
 * </p>
 */
public class TodayWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent serviceIntent = new Intent(context, WidgetService.class);
        context.startService(serviceIntent);
        loadWidgetImages(context);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WeatherMasterSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, WidgetService.class);
            context.startService(serviceIntent);
            loadWidgetImages(context);
            if (Utils.getNotificationPreference(context)) {
                notifyWeather(context);
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void loadWidgetImages(Context context) {
        String preferredLocation = Utils.getPreferredLocation(context);
        String iconName;
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        if (preferredLocation.trim().equals("")) {
            preferredLocation = "#";
        }
        preferredLocation = preferredLocation.toUpperCase();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                preferredLocation, System.currentTimeMillis());
        Cursor weatherCursor = context.getContentResolver().query(weatherForLocationUri,
                Constants.FORECAST_COLUMNS, null, null, sortOrder);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                TodayWidgetProvider.class));
        RemoteViews views;
        AppWidgetTarget appWidgetTarget;
        views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_large);
        if (appWidgetIds.length > 0) {
            appWidgetTarget = new AppWidgetTarget(context, views, R.id.widget_icon, appWidgetIds);
            if (!CursorUtils.isCursorEmpty(weatherCursor) && weatherCursor.moveToFirst()) {
                iconName = weatherCursor.getString(Constants.COL_ICON_NAME);
            } else {
                iconName = "";
            }
            Utils.loadImage(context, iconName, appWidgetTarget);
        }
    }

    public void notifyWeather(Context context) {
        //checking the last update and notify if it' the first of the day
        long currentTimestamp = System.currentTimeMillis();
        Calendar calDateTime;
        boolean isTimeSuitableForNotification;

        calDateTime = Calendar.getInstance();
        calDateTime.set(Calendar.MINUTE, 0); //Setting current minute to 0
        calDateTime.set(Calendar.SECOND, 0); //Setting current second to 0
        calDateTime.set(Calendar.MILLISECOND, 0); //Setting current millisecond to 0
        if (Utils.getDayConstant(context).equals(Constants.MORNING_CONSTANT)) {
            //Setting current hour to 8 AM
            calDateTime.set(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_DAY_THRESHOLD);
            //before 8 AM
            isTimeSuitableForNotification = currentTimestamp <= calDateTime.getTimeInMillis();
        } else {
            //Setting current hour to 8 PM
            calDateTime.set(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_NIGHT_THRESHOLD);
            //after 8 PM
            isTimeSuitableForNotification = currentTimestamp >= calDateTime.getTimeInMillis();
        }

        if (isTimeSuitableForNotification) {

            String locationQuery = Utils.getPreferredLocation(context);
            if (locationQuery.trim().equals("")) {
                locationQuery = "#";
            }
            locationQuery = locationQuery.toUpperCase();
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationQuery, System.currentTimeMillis());
            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri,
                    Constants.FORECAST_COLUMNS, null, null, null);
            if (!CursorUtils.isCursorEmpty(cursor)) {
                //If current time is after 8 PM then show tomorrow weather
                if (Utils.getDayConstant(context).equals(Constants.NIGHT_CONSTANT)) {
                    cursor.moveToPosition(1);
                }
                //If current time is before 8 AM then show today weather
                else {
                    cursor.moveToFirst();
                }
                double high = cursor.getDouble(Constants.COL_WEATHER_MAX_TEMP);
                double low = cursor.getDouble(Constants.COL_WEATHER_MIN_TEMP);
                String desc = cursor.getString(Constants.COL_WEATHER_DESC);
                String iconName = cursor.getString(Constants.COL_ICON_NAME);
                String date = Utils.getFriendlyDayString(context,
                        cursor.getLong(Constants.COL_WEATHER_DATE));
                String cityName = cursor.getString(Constants.COL_CITY_NAME);
                String contentText;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    contentText = String.format(context.getString(R.string.format_notification),
                            cityName,
                            date,
                            desc,
                            Utils.formatTemperature(context, high, Utils.isMetric(context)),
                            Utils.formatTemperature(context, low, Utils.isMetric(context)));
                } else {
                    contentText = String.format(context.getString(
                            R.string.format_notification_pre16), date, desc);
                }

                final RemoteViews rv = new RemoteViews(context.getPackageName(),
                        R.layout.custom_notification);
                rv.setTextViewText(R.id.notify_body, contentText);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContent(rv);

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(context, MainActivity.class);
                resultIntent.setData(weatherUri);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
                builder.setAutoCancel(true);
                final Notification notification = builder.build();
                // set big content view for newer androids
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification.bigContentView = rv;
                } else {
                    notification.contentView = rv;
                }

                NotificationTarget notificationTarget;
                notificationTarget = new NotificationTarget(
                        context,
                        rv,
                        R.id.notify_icon,
                        notification,
                        Constants.WEATHER_NOTIFICATION_ID);
                Utils.loadImage(context, iconName, notificationTarget);

                NotificationManager mNotificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(Constants.WEATHER_NOTIFICATION_ID, notification);

                if (Utils.getDayConstant(context).equals(Constants.NIGHT_CONSTANT)) {
                    Utils.setDayConstant(context, Constants.MORNING_CONSTANT);
                } else {
                    Utils.setDayConstant(context, Constants.NIGHT_CONSTANT);
                }
            }

        }

    }
}
