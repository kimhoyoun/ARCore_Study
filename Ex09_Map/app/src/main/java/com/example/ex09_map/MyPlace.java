package com.example.ex09_map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class MyPlace {
    String title;
    LatLng latLng;
    double latitude, longitude;

    double[] arPos;

    int color;

    MyPlace(String title, double latitude, double longitude, int color){
        this.title = title;
        this.latLng = new LatLng(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;

    }

    int rate = 2;
    void setArPosition(Location currentLocation, float[] mePos){
        arPos = new double[]{
                // x축 경도
                (mePos[0] + currentLocation.getLongitude() - longitude)*rate,
                (mePos[1] + currentLocation.getLatitude() - latitude)*rate,
                mePos[2]
        };
    }

}
