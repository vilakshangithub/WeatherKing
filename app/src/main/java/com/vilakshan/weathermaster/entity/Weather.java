package com.vilakshan.weathermaster.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * <p>
 * Created by vilakshan on 12/12/16.
 * </p>
 */

public class Weather implements Parcelable {

    @SerializedName("id")
    public int weatherId;

    @SerializedName("main")
    public String weatherDescription;

    @SerializedName("description")
    public String weatherFullDescription;

    @SerializedName("icon")
    public String weatherIcon;

    protected Weather(Parcel in) {
        weatherId = in.readInt();
        weatherDescription = in.readString();
        weatherFullDescription = in.readString();
        weatherIcon = in.readString();
    }

    public static final Creator<Weather> CREATOR = new Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel in) {
            return new Weather(in);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(weatherId);
        parcel.writeString(weatherDescription);
        parcel.writeString(weatherFullDescription);
        parcel.writeString(weatherIcon);
    }
}
