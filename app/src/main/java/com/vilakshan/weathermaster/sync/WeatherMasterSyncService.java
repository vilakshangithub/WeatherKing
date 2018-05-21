package com.vilakshan.weathermaster.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WeatherMasterSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static WeatherMasterSyncAdapter sWeatherMasterSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sWeatherMasterSyncAdapter == null) {
                sWeatherMasterSyncAdapter = new WeatherMasterSyncAdapter(
                        getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sWeatherMasterSyncAdapter.getSyncAdapterBinder();
    }
}