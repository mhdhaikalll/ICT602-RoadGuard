package com.example.roadguard.util;


import com.firebase.geofire.core.GeoHash;

public class GeohashHelper {
    public static String encode(double lat, double lng, int precision) {
        return new GeoHash(lat, lng, precision).getGeoHashString();
    }
}
