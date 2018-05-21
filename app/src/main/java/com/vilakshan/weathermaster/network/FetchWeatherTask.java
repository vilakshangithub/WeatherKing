package com.vilakshan.weathermaster.network;

import android.content.Context;
import android.os.AsyncTask;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.views.CustomProgressDialog;

public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

    private CustomProgressDialog mProgressDialog = null;
    private final Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new CustomProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setMessage(mContext.getString(R.string.progressDialogMessage));
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        WeatherRequestHelper weatherRequestHelper = new WeatherRequestHelper(mContext);
        weatherRequestHelper.getWeatherResponse();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mProgressDialog.dismiss();
    }
}
