package com.vilakshan.weathermaster.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.data.WeatherContract;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.CursorUtils;
import com.vilakshan.weathermaster.utils.Utils;

/**
 * A placeholder mFragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mWeatherString = "";
    Uri mUri = null;
    final int DETAIL_LOADER = 1;
    public static final String DETAIL_URI = "URI";
    static final String DETAIL_FINAL_ANIMATION = "DTA";
    private View mRootView = null;
    private ShareActionProvider mShareActionProvider;
    private boolean mTransitionAnimation = false;
    private String mTransitionId;

    public DetailActivityFragment() {
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        if (newLocation.trim().equals("")) {
            newLocation = "#";
        }
        newLocation = newLocation.toUpperCase();
        Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocation(newLocation);
        if (null != updatedUri) {
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    public void OnTempUnitChanged() {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    // Call to create the share intent
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mWeatherString + "@" + getContext().getString(R.string.app_name));
        return shareIntent;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            return true;
        } else if (id == android.R.id.home) {
            Intent up = NavUtils.getParentActivityIntent(getActivity());
            up.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(getActivity(), up);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DETAIL_URI);
            mTransitionAnimation = args.getBoolean(DETAIL_FINAL_ANIMATION);
            mTransitionId = args.getString(MainActivity.TRANSITION_NAME_TAG);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ViewHolder viewHolder = new ViewHolder(mRootView);
        mRootView.setTag(viewHolder);
        return mRootView;
    }

    private class ViewHolder {
        final ImageView imgIcon;
        final TextView cityNameTV;
        final TextView dateTV;
        final TextView forecastTV;
        final TextView highTempTV;
        final TextView lowTempTV;
        final TextView windTV;
        final TextView humidityTV;
        final TextView pressureTV;

        ViewHolder(View view) {
            imgIcon = (ImageView) view.findViewById(R.id.weather_item_icon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgIcon.setTransitionName(mTransitionId);
            }
            cityNameTV = (TextView) view.findViewById(R.id.detail_City_Name);
            dateTV = (TextView) view.findViewById(R.id.weather_item_text_view);
            forecastTV = (TextView) view.findViewById(R.id.weather_item_forecast_textview);
            highTempTV = (TextView) view.findViewById(R.id.weather_item_high_textview);
            lowTempTV = (TextView) view.findViewById(R.id.weather_item_low_textview);
            windTV = (TextView) view.findViewById(R.id.wind_text_view);
            humidityTV = (TextView) view.findViewById(R.id.humidity_text_view);
            pressureTV = (TextView) view.findViewById(R.id.pressure_text_view);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), mUri, Constants.FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Cursor data returned with pointer to -1 in On Load Finished
        double high, low, humidity, pressure;
        float windSpeed, degrees;
        String desc, date, cityName, iconName;
        int weatherId;
        ViewHolder viewHolder = (ViewHolder) mRootView.getTag();
        if (!CursorUtils.isCursorEmpty(data) && data.moveToFirst()) {
            //Setting city name
            cityName = data.getString(Constants.COL_CITY_NAME);
            //Setting Date
            date = Utils.getFriendlyDayString(getContext(), data.getLong(
                    Constants.COL_WEATHER_DATE));
            //Setting High Temp
            high = data.getDouble(Constants.COL_WEATHER_MAX_TEMP);
            viewHolder.highTempTV.setText(Utils.formatTemperature(getContext(),
                    high, Utils.isMetric(getContext())));
            //Setting Low Temp
            low = data.getDouble(Constants.COL_WEATHER_MIN_TEMP);
            viewHolder.lowTempTV.setText(Utils.formatTemperature(getContext(),
                    low, Utils.isMetric(getContext())));
            weatherId = data.getInt(Constants.COL_WEATHER_CONDITION_ID);
            iconName = data.getString(Constants.COL_ICON_NAME);
            //Setting Forecast
            desc = Utils.getDetailedDescForWeatherCondition(getContext(), weatherId);
            //Setting Humidity
            humidity = data.getDouble(Constants.COL_HUMIDITY);
            //Setting Pressure
            pressure = data.getDouble(Constants.COL_PRESSURE);
            //Setting Wind Speed
            windSpeed = data.getFloat(Constants.COL_WIND_SPEED);
            degrees = data.getFloat(Constants.COL_DEGREES);

            mWeatherString = String.format(getContext().getString(R.string.format_notification),
                    cityName,
                    date,
                    desc,
                    Utils.formatTemperature(getContext(), high, Utils.isMetric(getContext())),
                    Utils.formatTemperature(getContext(), low, Utils.isMetric(getContext())));
        } else {
            cityName = date = iconName = desc = "";
            humidity = pressure = -1.0;
            windSpeed = degrees = 0.0f;
            viewHolder.highTempTV.setText("");
            viewHolder.lowTempTV.setText("");
        }

        viewHolder.cityNameTV.setText(cityName);
        viewHolder.dateTV.setText(date);
        //Setting Image
        Utils.loadImage(getContext(), iconName, viewHolder.imgIcon);
        viewHolder.forecastTV.setText(desc);
        if (humidity == -1.0) {
            viewHolder.humidityTV.setText("");
        } else {
            viewHolder.humidityTV.setText(Utils.getFormattedHumidity(getContext(), humidity));
        }
        if (pressure == -1.0) {
            viewHolder.pressureTV.setText("");
        } else {
            viewHolder.pressureTV.setText(Utils.getFormattedPressure(getContext(), pressure));
        }
        if (windSpeed == 0.0f || degrees == 0.0f) {
            viewHolder.windTV.setText("");
        } else {
            viewHolder.windTV.setText(Utils.getFormattedWind(getContext(), windSpeed, degrees));
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

