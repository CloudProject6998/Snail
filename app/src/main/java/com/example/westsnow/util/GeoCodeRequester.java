package com.example.westsnow.util;


import org.json.JSONObject;
import java.io.IOException;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.*;
/**
 * Created by yingtan on 5/19/15.
 */
public class GeoCodeRequester {

    public JSONObject m_jsonObj;
    public String m_returnedJson;

    private static GeoCodeRequester m_instance = null;

    private GeoCodeRequester(){

    }

    public static GeoCodeRequester getInstance(){
        if(m_instance == null){
            m_instance = new GeoCodeRequester();
        }
        return m_instance;
    }

    public LatLng getGeoLocation(Context context, String locationName){
        double latitude;
        double longitude;

        LatLng res = null;

        List<Address> geocodeMatches = null;

        try {
            geocodeMatches = new Geocoder(context).getFromLocationName(locationName, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ((geocodeMatches != null) && (!geocodeMatches.isEmpty())) {
            latitude = geocodeMatches.get(0).getLatitude();
            longitude = geocodeMatches.get(0).getLongitude();

            res = new LatLng(latitude,longitude);
            //Todo: show to user  +  save to DB
            System.out.println("GeoCoding:"+latitude +" , "+longitude);
        }
        return res;
    }


}
