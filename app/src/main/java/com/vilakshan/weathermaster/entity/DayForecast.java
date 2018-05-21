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

public class DayForecast implements Parcelable {

    @SerializedName("pressure")
    public double pressure;

    @SerializedName("humidity")
    public double humidity;

    @SerializedName("speed")
    public double windSpeed;

    @SerializedName("deg")
    public double windDirection;

    @SerializedName("temp")
    public Temperature temperature;

    @SerializedName("weather")
    public ArrayList<Weather> weatherList;

    protected DayForecast(Parcel in) {
        pressure = in.readDouble();
        humidity = in.readDouble();
        windSpeed = in.readDouble();
        windDirection = in.readDouble();
        temperature = in.readParcelable(Temperature.class.getClassLoader());
        weatherList = in.createTypedArrayList(Weather.CREATOR);
    }

    public static final Creator<DayForecast> CREATOR = new Creator<DayForecast>() {
        @Override
        public DayForecast createFromParcel(Parcel in) {
            return new DayForecast(in);
        }

        @Override
        public DayForecast[] newArray(int size) {
            return new DayForecast[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(pressure);
        parcel.writeDouble(humidity);
        parcel.writeDouble(windSpeed);
        parcel.writeDouble(windDirection);
        parcel.writeParcelable(temperature, i);
        parcel.writeTypedList(weatherList);
    }
}
