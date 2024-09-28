package com.store.neareststores;

import android.os.Parcel;
import android.os.Parcelable;

public class Store implements Parcelable {
    private final String name;
    private final double latitude;
    private final double longitude;
    private String location;

    public Store(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(location);
    }

    public static final Parcelable.Creator<Store> CREATOR = new Parcelable.Creator<Store>() {
        @Override
        public Store createFromParcel(Parcel in) {
            String name = in.readString();
            double latitude = in.readDouble();
            double longitude = in.readDouble();
            String location = in.readString();
            Store store = new Store(name, latitude, longitude);
            store.setLocation(location);
            return store;
        }

        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };
}
