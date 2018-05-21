package com.vilakshan.weathermaster.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * <p>
 * Created by vilakshan on 12/12/16.
 * </p>
 */

public class Temperature implements Parcelable {

    @SerializedName("max")
    public double maxTemperature;

    @SerializedName("min")
    public double minTemperature;

    protected Temperature(Parcel in) {
        maxTemperature = in.readDouble();
        minTemperature = in.readDouble();
    }

    public static final Creator<Temperature> CREATOR = new Creator<Temperature>() {
        @Override
        public Temperature createFromParcel(Parcel in) {
            return new Temperature(in);
        }

        @Override
        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(maxTemperature);
        parcel.writeDouble(minTemperature);
    }
}
