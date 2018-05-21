package com.vilakshan.weathermaster.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * <p>
 * Created by vilakshan on 12/12/16.
 * </p>
 */

public class WeatherData implements Parcelable {

    @SerializedName("cod")
    public int responseCode;

    @SerializedName("city")
    public City city;

    @SerializedName("list")
    public ArrayList<DayForecast> dayForecastList;

    protected WeatherData(Parcel in) {
        responseCode = in.readInt();
        city = in.readParcelable(City.class.getClassLoader());
        dayForecastList = in.createTypedArrayList(DayForecast.CREATOR);
    }

    public static final Creator<WeatherData> CREATOR = new Creator<WeatherData>() {
        @Override
        public WeatherData createFromParcel(Parcel in) {
            return new WeatherData(in);
        }

        @Override
        public WeatherData[] newArray(int size) {
            return new WeatherData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(responseCode);
        dest.writeParcelable(city, flags);
        dest.writeTypedList(dayForecastList);
    }
}
