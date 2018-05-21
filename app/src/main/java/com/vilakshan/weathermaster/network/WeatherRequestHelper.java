package com.vilakshan.weathermaster.network;

import android.content.Context;
import android.net.Uri;

import com.vilakshan.weathermaster.BuildConfig;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>
 * Created by vilakshan on 12/12/16.
 * </p>
 */

public class WeatherRequestHelper {

    private static final String FORECAST_BASE_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String PARAM_LOCATION = "q";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_UNITS = "units";
    private static final String PARAM_DAY_COUNT = "cnt";
    private static final String PARAM_APPLICATION_ID = "APPID";
    private static final String METHOD_GET = "GET";

    private static final String VALUE_MODE = "json";
    private static final String VALUE_UNITS = "metric";
    private static final String VALUE_DAY_COUNT = "14";

    private Context mContext;

    public WeatherRequestHelper(Context context) {
        mContext = context;
    }

    public void getWeatherResponse() {
        String locationQuery = Utils.getPreferredLocation(mContext);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            String apiKey = Utils.getAPIKey(mContext);
            String finalOWMKey = "";
            switch (apiKey) {
                case Constants.API_KEY_1:
                    finalOWMKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                    Utils.setAPIKey(mContext, Constants.API_KEY_2);
                    break;
                case Constants.API_KEY_2:
                    finalOWMKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY_2;
                    Utils.setAPIKey(mContext, Constants.API_KEY_3);
                    break;
                case Constants.API_KEY_3:
                    finalOWMKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY_3;
                    Utils.setAPIKey(mContext, Constants.API_KEY_1);
                    break;
            }

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_LOCATION, locationQuery)
                    .appendQueryParameter(PARAM_MODE, VALUE_MODE)
                    .appendQueryParameter(PARAM_UNITS, VALUE_UNITS)
                    .appendQueryParameter(PARAM_DAY_COUNT, VALUE_DAY_COUNT)
                    .appendQueryParameter(PARAM_APPLICATION_ID, finalOWMKey)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(METHOD_GET);
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                forecastJsonStr = buffer.toString();
                Utils.saveWeatherDataFromResponse(mContext, forecastJsonStr);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
