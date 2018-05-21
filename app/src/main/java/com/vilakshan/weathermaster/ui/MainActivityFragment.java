package com.vilakshan.weathermaster.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.adapters.RecForecastAdapter;
import com.vilakshan.weathermaster.data.WeatherContract;
import com.vilakshan.weathermaster.location.FetchGPSLocation;
import com.vilakshan.weathermaster.network.FetchWeatherTask;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.CursorUtils;
import com.vilakshan.weathermaster.utils.Utils;

import java.util.List;

/**
 * A placeholder mFragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    RecyclerView mRecWeatherForecast = null;
    ImageView mEmptyForecastImage = null;
    RecForecastAdapter mForecastAdapter = null;
    private final static String SCROLL_POSITION = "SCROLL";
    final static int FORECAST_LOADER = 0;
    int mPosition = RecyclerView.NO_POSITION;
    boolean mUseTodayLayout, mHasLocationPermission;
    private boolean mHoldForTransition = false;
    private TextView mEmptyView;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainActivityFragment,
                0, 0);
        mHoldForTransition = a.getBoolean(R.styleable.MainActivityFragment_sharedElementTransitions,
                false);
        a.recycle();
    }

    /**
     * A callback interface that all activities containing this mFragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri, String transitionName, ImageView imgIcon);
    }

    public MainActivityFragment() {
    }

    //Public Setter Method
    public void setTodayLayoutFlag(boolean todayFlag) {
        mUseTodayLayout = todayFlag;
        if (mForecastAdapter != null) {
            mForecastAdapter.setTodayLayoutFlag(todayFlag);
        }
    }

    @Override
    public void onResume() {
        SharedPreferences prefLocation = PreferenceManager.getDefaultSharedPreferences(
                getContext());
        prefLocation.registerOnSharedPreferenceChangeListener(this);
        super.onResume();

    }

    @Override
    public void onPause() {
        SharedPreferences prefLocation = PreferenceManager.getDefaultSharedPreferences(
                getContext());
        prefLocation.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void updateWeatherData() {
        FetchWeatherTask fetchWhetherTask;
        fetchWhetherTask = new FetchWeatherTask(getActivity());
        fetchWhetherTask.execute();
    }

    public void fetchCurrentLocationFromGPS() {
        FetchGPSLocation gpsAsync;
        mHasLocationPermission = false;
        if (Utils.hasMarshMallow()) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.GPS_PERMISSION_CODE);
            } else {
                mHasLocationPermission = true;
            }

        } else {
            mHasLocationPermission = true;
        }
        if (mHasLocationPermission) {
            if (Utils.isGPSEnabled(getContext())) {
                gpsAsync = new FetchGPSLocation(getContext());
                gpsAsync.execute();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.gpsNotEnabled));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.gpsYes),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int id) {
                                final Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(getString(R.string.gpsNo),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.GPS_PERMISSION_CODE:
                if (grantResults.length == 1 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    mHasLocationPermission = true;
                    fetchCurrentLocationFromGPS();
                } else {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        Utils.showPermissionMessageForMarshMallow(getActivity(), permissions[0]);
                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openPreferredLocationOnMap() {
        if (mForecastAdapter != null) {
            Cursor cursor = mForecastAdapter.getCursor();
            if (!CursorUtils.isCursorEmpty(cursor) && cursor.moveToFirst()) {
                String latitude = cursor.getString(Constants.COL_COORD_LAT);
                String longitude = cursor.getString(Constants.COL_COORD_LONG);
                String cityName = cursor.getString(Constants.COL_CITY_NAME);
                Intent intent;
                intent = new Intent(Intent.ACTION_VIEW);
                Uri geoLocation = Uri.parse("geo:" + latitude + "," + longitude + "?q=" +
                        latitude + "," + longitude + "("
                        + cityName + ")");
                intent.setData(geoLocation);
                PackageManager packageManager = getActivity().getPackageManager();
                List activities = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    startActivity(intent);
                } else {
                    Utils.showToastMessage(getContext(),
                            getContext().getString(R.string.no_valid_application),
                            Toast.LENGTH_SHORT);
                }
            }
        } else {
            Utils.showToastMessage(getContext(), getString(R.string.empty_weather_string),
                    Toast.LENGTH_SHORT);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            if (Utils.IsConnected(getContext())) {
                updateWeatherData();
            } else {
                Utils.showToastMessage(getContext(), getString(R.string.no_internet_weather_string),
                        Toast.LENGTH_SHORT);
            }
        } else if (id == R.id.action_gps) {
            if (Utils.IsConnected(getContext())) {
                fetchCurrentLocationFromGPS();
            } else {
                Utils.showToastMessage(getContext(), getString(R.string.no_internet),
                        Toast.LENGTH_SHORT);
            }

        } else if (id == R.id.action_maps) {
            if (Utils.IsConnected(getContext())) {
                openPreferredLocationOnMap();
            } else {
                Utils.showToastMessage(getContext(), getString(R.string.no_internet),
                        Toast.LENGTH_SHORT);
            }

        }
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SCROLL_POSITION, mPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecWeatherForecast = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        mRecWeatherForecast.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEmptyView = (TextView) rootView.findViewById(R.id.listview_forecast_empty);
        /* setting to improve performance if you know that changes
         in content do not change the layout size of the RecyclerView*/
        mRecWeatherForecast.setHasFixedSize(true);
        mEmptyForecastImage = (ImageView) rootView.findViewById(R.id.image_forecast_empty);

        mForecastAdapter = new RecForecastAdapter(getContext(), new RecForecastAdapter.RecForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, RecForecastAdapter.ViewHolder vh, View v) {
                String locationSetting = Utils.getPreferredLocation(getActivity());
                locationSetting = locationSetting.toUpperCase();
                ImageView imgIcon = (ImageView) v.findViewById(R.id.list_item_icon);
                ((Callback) getActivity()).onItemSelected(WeatherContract.
                                WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                        date),
                        Constants.MAIN_ICON_TRANSITION_STRING + vh.getAdapterPosition(), imgIcon);
                mPosition = vh.getAdapterPosition();
            }
        }, mEmptyView);

        mForecastAdapter.setTodayLayoutFlag(mUseTodayLayout);
        mRecWeatherForecast.setAdapter(mForecastAdapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION)) {
            mPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }
        return rootView;
    }

    public void onLocationChanged() {
        updateWeatherData(); //Call Open Weather Map API
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    public void onTempUnitChanged() {
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utils.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        if (locationSetting.trim().equals("")) {
            locationSetting = "#";
        }
        locationSetting = locationSetting.toUpperCase();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri,
                Constants.FORECAST_COLUMNS, null, null, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateEmptyView(data);
        mForecastAdapter.swapCursor(data);
        if (data.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
            mEmptyForecastImage.setVisibility(View.VISIBLE);
        } else {
            if (mHoldForTransition) {
                getActivity().supportStartPostponedEnterTransition();
            }
            mEmptyForecastImage.setVisibility(View.INVISIBLE);
        }

        if (mPosition != RecyclerView.NO_POSITION) {
            mRecWeatherForecast.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void updateEmptyView(Cursor data) {
        if (CursorUtils.isCursorEmpty(data)) {
            int message = R.string.empty_weather_string;
            @Utils.LocationStatus int location = Utils.getLocationStatus(getContext());
            switch (location) {
                case Utils.LOCATION_STATUS_SERVER_DOWN:
                    message = R.string.empty_forecast_list_server_down;
                    break;
                case Utils.LOCATION_STATUS_SERVER_INVALID:
                    message = R.string.empty_forecast_list_server_error;
                    break;
                case Utils.LOCATION_STATUS_INVALID:
                    message = R.string.empty_forecast_invalid_location;
                    break;
                default:
                    if (!Utils.IsConnected(getContext())) {
                        message = R.string.no_internet_weather_string;
                    }
            }
            mEmptyView.setText(message);
        }
    }

}

