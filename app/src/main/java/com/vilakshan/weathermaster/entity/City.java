package com.vilakshan.weathermaster.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * <p>
 * Created by vilakshan on 12/12/16.
 * </p>
 */

public class City implements Parcelable {

    @SerializedName("name")
    public String name;

    @SerializedName("coord")
    public Coordinate coordinate;

    protected City(Parcel in) {
        name = in.readString();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }
}
