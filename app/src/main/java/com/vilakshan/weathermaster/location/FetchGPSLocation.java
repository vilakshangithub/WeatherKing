package com.vilakshan.weathermaster.location;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.ui.SettingsActivity;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.views.CustomProgressDialog;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by VilakshanSaxena on 12/19/2015.
 */
public class FetchGPSLocation extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private LocationManager mLocationManager;
    private Geocoder mGcd;
    private String mFullGeoLocation = "";
    private MyListener mList;
    private boolean mGpsStatus;
    private CustomProgressDialog mProgressDialog = null;
    private AlertDialog.Builder mBuilder;

    public FetchGPSLocation(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //location mLocationManager
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        //Alert Dialog Initialization
        mBuilder = new AlertDialog.Builder(mContext);
        mList = new MyListener();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mList);
        mGcd = new Geocoder(mContext, Locale.getDefault());

        //Alert Dialog
        mBuilder.setMessage(mContext.getString(R.string.gpsMessage));
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton(mContext.getString(R.string.gpsRetry), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,
                                int id) {
                dialog.cancel();
                new FetchGPSLocation(mContext).execute();
            }
        });
        mBuilder.setNegativeButton(mContext.getString(R.string.gpsAbort), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,
                                int id) {
                dialog.cancel();
            }
        });

        mProgressDialog = new CustomProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setMessage(mContext.getString(R.string.progressDialogGPSMessage));
    }

    @Override
    protected Void doInBackground(Void... params) {
        mGpsStatus = true;
        Date d1 = new Date();
        long StartTime = d1.getTime();
        long EndTime;
        while (TextUtils.isEmpty(mFullGeoLocation)) {
            Date d2 = new Date();
            EndTime = d2.getTime();
            long diff = EndTime - StartTime;
            if (diff >= Constants.GPS_DURATION) {
                mGpsStatus = false;
                break;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // dismiss the dialog once done
        mProgressDialog.dismiss();
        mLocationManager.removeUpdates(mList);
        //GPS location is not fetched
        if (!mGpsStatus) {
            mGpsStatus = true;
            AlertDialog alert = mBuilder.create();
            alert.show();
        } else {
            //GPS location successfully fetched, store location in preference and navigate
            // to Settings screen
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(mContext.getString(R.string.pref_key_basic), mFullGeoLocation);
            editor.apply();
            Intent intent = new Intent(mContext, SettingsActivity.class);
            mContext.startActivity(intent);
        }
    }

    private class MyListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Double mLatitude = location.getLatitude();
            Double mLongitude = location.getLongitude();
            try {
                List<Address> addresses = mGcd.getFromLocation(mLatitude,
                        mLongitude, 1);
                if (addresses.size() > 0)

                    mFullGeoLocation = "" + addresses.get(0).getLocality(); //Fetching City Name
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
            // TODO Auto-generated method stub

        }

    }
}
