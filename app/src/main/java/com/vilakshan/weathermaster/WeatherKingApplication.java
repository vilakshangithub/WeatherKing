package com.vilakshan.weathermaster;

import android.app.Application;
import com.vilakshan.weathermaster.sync.WeatherMasterSyncAdapter;

/**
 * Created by vilakshan on 3/1/16.
 */
public class WeatherKingApplication extends Application {

    public WeatherKingApplication() {
        super();
    }

    @Override
    public void onCreate() {
        WeatherMasterSyncAdapter.initializeSyncAdapter(this);
        super.onCreate();
    }
}
