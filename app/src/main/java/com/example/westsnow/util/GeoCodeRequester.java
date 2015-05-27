package com.example.westsnow.util;


import org.json.JSONObject;
import java.io.IOException;

import android.content.Context;
import android.location.Address;
import android.location.*;

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

    public double[] getStartEndLocation(Context context, String startValue, String endValue, Location lastLoc){

        MapUtil util = MapUtil.getInstance();
        double[] startEndLocs = new double[4];
        LatLng startLoca = null;

        if(startValue.equals("")) {
            startEndLocs[0] = lastLoc.getLatitude();
            startEndLocs[1] = lastLoc.getLongitude();

            startLoca = new LatLng(startEndLocs[0],startEndLocs[1]);
        }
        else{
            String startPosName = util.formatInputLoca(startValue);
            startLoca = getGeoLocation(context, startPosName);
            startEndLocs[0] = startLoca.latitude;
            startEndLocs[1] = startLoca.longitude;
        }

        String endPosName = util.formatInputLoca(endValue);
        LatLng endLoca = getGeoLocation(context, endPosName);

        startEndLocs[2] = endLoca.latitude;
        startEndLocs[3] = endLoca.longitude;

        if(endLoca == null){
            // no input of destination and do tracker- > wrong
        }
        sendMessage2Listener(startLoca,endLoca);
        return startEndLocs;

    }

    public void sendMessage2Listener(LatLng startLoca, LatLng endLoca){

        LocaChangeTracker.m_startLocation = startLoca;
        LocaChangeTracker.m_endLocation = endLoca;
        CurLocaTracker.m_startLocation = startLoca;
        CurLocaTracker.m_endLocation = endLoca;
    }



}
