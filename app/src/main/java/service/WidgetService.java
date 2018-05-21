package service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.ui.MainActivity;
import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.widget.TodayWidgetProvider;
import com.vilakshan.weathermaster.utils.Utils;
import com.vilakshan.weathermaster.data.WeatherContract;

/**
 * Created by Vilakshan Saxena on 11/15/2015.
 */
public class WidgetService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private final Context mContext = this;

    public WidgetService() {
        super(String.valueOf(WidgetService.class.getSimpleName()));
    }

    public WidgetService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String preferredLocation = Utils.getPreferredLocation(mContext);
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        if (preferredLocation.trim().equals("")) {
            preferredLocation = "#";
        }
        preferredLocation = preferredLocation.toUpperCase();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                preferredLocation, System.currentTimeMillis());
        Cursor weatherCur = mContext.getContentResolver().query(weatherForLocationUri,
                Constants.FORECAST_COLUMNS, null, null, sortOrder);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, TodayWidgetProvider.class));
        Intent triggerIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, triggerIntent, 0);
        RemoteViews views;
        views = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_large);
        views.setOnClickPendingIntent(R.id.widget_weather, pendingIntent);

        if (weatherCur.moveToFirst()) {
            int weatherId = weatherCur.getInt(Constants.COL_WEATHER_CONDITION_ID);
            Double highTemp = weatherCur.getDouble(Constants.COL_WEATHER_MAX_TEMP);
            Double lowTemp = weatherCur.getDouble(Constants.COL_WEATHER_MIN_TEMP);
            String weatherDesc = Utils.getDescForWeatherCondition(mContext,weatherId);
            String iconName = weatherCur.getString(Constants.COL_ICON_NAME);
            String weatherLocationName = weatherCur.getString(Constants.COL_CITY_NAME);

            for (int appWidgetId : appWidgetIds) {
                        /*Setting 4x1 Widget Field values*/
                views.setTextViewText(R.id.widget_weather_loc, weatherLocationName);
                views.setTextViewText(R.id.widget_weather_desc, weatherDesc);
                views.setTextViewText(R.id.widget_high_temp,
                        Utils.formatTemperature(mContext, highTemp, Utils.isMetric(mContext)));
                views.setTextViewText(R.id.widget_low_temp,
                        Utils.formatTemperature(mContext, lowTemp, Utils.isMetric(mContext)));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

        }
        else
        {
            for (int appWidgetId : appWidgetIds) {
                        /*Setting 4x1 Widget Field values*/
                views.setTextViewText(R.id.widget_weather_loc, "-");
                views.setTextViewText(R.id.widget_weather_desc, "-");
                views.setTextViewText(R.id.widget_high_temp,
                        "-");
                views.setTextViewText(R.id.widget_low_temp,
                        "-");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }

    }
}
