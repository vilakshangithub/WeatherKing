package com.vilakshan.weathermaster.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.data.WeatherContract;
import com.vilakshan.weathermaster.utils.Utils;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    public static final String DETAIL_FRAGMENT_TAG = "DF_TAG";
    public static final String TRANSITION_NAME_TAG = "TN";
    boolean mTwoPane = false;
    String mLocation = "";
    boolean mIsMetric;

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utils.getPreferredLocation(this);
        boolean isMetric = Utils.isMetric(this);
        MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment);
        DetailActivityFragment df = (DetailActivityFragment) getSupportFragmentManager().
                findFragmentByTag(DETAIL_FRAGMENT_TAG);

        // update the location in our second pane using the mFragment manager
        if (location != null && !location.equals(mLocation)) {
            if (null != ff) {
                ff.onLocationChanged();
            }
            if (null != df) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
        // if temperature unit is changed
        if (isMetric != mIsMetric) {
            if (null != ff) {
                ff.onTempUnitChanged();
            }
            if (null != df) {
                df.OnTempUnitChanged();
            }
            mIsMetric = isMetric;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mLocation = Utils.getPreferredLocation(this);
        mIsMetric = Utils.isMetric(this);
        MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment);
        if (findViewById(R.id.weather_detail_container) != null) {
            if (null != ff) {
                ff.setTodayLayoutFlag(false);
            }
            mTwoPane = true;
            if (savedInstanceState == null) {
                Bundle args = new Bundle();
                String locationSetting = Utils.getPreferredLocation(this);
                if (locationSetting.trim().equals("")) {
                    locationSetting = "#";
                }
                locationSetting = locationSetting.toUpperCase();
                args.putParcelable(DetailActivityFragment.DETAIL_URI,
                        WeatherContract.WeatherEntry.buildWeatherLocation(locationSetting));
                args.putBoolean(DetailActivityFragment.DETAIL_FINAL_ANIMATION, true);
                DetailActivityFragment detailFragment = new DetailActivityFragment();
                detailFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
            if (null != ff) {
                ff.setTodayLayoutFlag(true);
            }
        }
        //If sync account flag is not set yet
        if (!Utils.getSyncAccountCreatedFlag(this)) {
            if (null != ff) {
                ff.onLocationChanged();
            }
            Utils.setSyncAccountCreatedFlag(this, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent(this, SettingsActivity.class);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri, String transitionName, ImageView imgIcon) {
        if (mTwoPane) //Tablet
        {
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, dateUri);
            args.putBoolean(DetailActivityFragment.DETAIL_FINAL_ANIMATION, true);
            args.putString(TRANSITION_NAME_TAG, transitionName);
            DetailActivityFragment detailFragment = new DetailActivityFragment();
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment,
                            MainActivity.DETAIL_FRAGMENT_TAG)
                    .commit();
        } else //Phone
        {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            intent.putExtra(TRANSITION_NAME_TAG, transitionName);
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, imgIcon, transitionName);
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        }
    }
}
