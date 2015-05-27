package com.example.westsnow.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by wumengting on 5/23/15.
 */
public class Route {
    /*
     * Function: Get the top three closest existing routes according to the start and destination with the most posts of photos on the way
     * Input: startlat, startlong, deslat, deslong
     * Output: routeID list (three routes)
     */
    public static List<Long> m_recomRoutes = null;

    public List<Long> recommendRoutes(double startlat, double startlong, double deslat, double deslong) throws JSONException, ExecutionException, InterruptedException{
        List<Long> routes = new ArrayList<Long>();
        dbUtil db = dbUtil.getInstance();
        double maxone = Double.MIN_VALUE;
        double maxtwo = Double.MIN_VALUE;
        double maxthree = Double.MIN_VALUE;
        double distance_threshold = 0.005;
        long one = 0;
        long two = 0;
        long three = 0;

        JSONArray StartEndPairs = db.getAllStartEnd();
        if (StartEndPairs != null) {
            Log.d("getStartEndPairs", StartEndPairs.toString());
            for (int i = 0; i < StartEndPairs.length(); i++) {
                JSONObject c = StartEndPairs.getJSONObject(i);
                String routeID = c.getString("routeID");

                JSONArray posPairs = db.getRoute(routeID.toString());
                if((posPairs == null) || (posPairs.length() == 0))
                    continue;
                String userName = c.getString("userName");
                double sLatt = Double.valueOf(c.getString("sLatt"));
                double sLong = Double.valueOf(c.getString("sLong"));
                double eLatt = Double.valueOf(c.getString("eLatt"));
                double eLong = Double.valueOf(c.getString("eLong"));
                double likeAvg = Double.valueOf(c.getString("likeAvg"));
                double start_dis = Math.sqrt(Math.pow(startlat - sLatt, 2) + Math.pow(startlong - sLong, 2));
                double des_dis = Math.sqrt(Math.pow(deslat - eLatt, 2) + Math.pow(deslong - eLong, 2));
                double distance = start_dis + des_dis;


                System.out.println("*****routeID: " + routeID + " start_distance:" + start_dis + " des_distance:" + des_dis + " dis:" + distance + " likeAvg:" + likeAvg);

                if(distance > distance_threshold)
                    continue;

                double weighted_likeAvg = likeAvg - distance;

                System.out.println("*****routeID: " + routeID + " weighted_likAvg: " + weighted_likeAvg);

                if (weighted_likeAvg > maxone) {
                    maxthree = maxtwo;
                    maxtwo = maxone;
                    maxone = weighted_likeAvg;
                    three = two;
                    two = one;
                    one = Long.valueOf(routeID);
                } else if (weighted_likeAvg > maxtwo) {
                    maxthree = maxtwo;
                    maxtwo = weighted_likeAvg;
                    three = two;
                    two = Long.valueOf(routeID);
                } else if (weighted_likeAvg < maxthree) {
                    maxthree = weighted_likeAvg;
                    three = Long.valueOf(routeID);
                }
            }
            if (one != 0) {
                routes.add(one);
            }
            if (two != 0) {
                routes.add(two);
            }
            if (three != 0) {
                routes.add(three);
            }
        } else {
            Log.d("getStartEndPairs", "null");
        }

        System.out.println("*****selected routeIDs: " + routes);
        m_recomRoutes = routes;
        return routes;
    }

 /*
 * Function: Get all the points on the route according to routeID
 * Input: routeID
 * Output: points list (latitude & longitude)
 */

    public List<LatLng> routePoints(Long routeID) throws JSONException, ExecutionException, InterruptedException{
        List<LatLng> pointList = new ArrayList<LatLng>();
        dbUtil db = dbUtil.getInstance();


        JSONArray posPairs = db.getRoute(routeID.toString());

        if (posPairs != null) {
            Log.d("getPositions", posPairs.toString());
            for (int i = 0; i < posPairs.length(); i++) {
                JSONObject c = posPairs.getJSONObject(i);
                double latitude = Double.valueOf(c.getString("latitude"));
                double longitude = Double.valueOf(c.getString("longitude"));
                LatLng location = new LatLng(latitude, longitude);

                pointList.add(location);

                Log.d("latt", String.valueOf(latitude));
                Log.d("long", String.valueOf(longitude));
            }
        } else {
            Log.d("getPositions", "null");
        }
        return pointList;
    }


 /*
 * Function: Insert a new route into db
 * Input: username, startlatt, startlong, deslatt, deslong
 * Output: routeID
 */

    public long createNewRoute(dbUtil db, String username, double startlatt, double startlong, double deslatt, double deslong) throws JSONException, ExecutionException, InterruptedException{
        long routeID = Long.valueOf(db.insertStartEnd(username, String.valueOf(startlatt), String.valueOf(startlong), String.valueOf(deslatt), String.valueOf(deslong)));

        return routeID;
    }


/*
 * Function: Insert points for a routeID into db
 * Input: routeID, latt, long
 * Output: void
 */

    public void addPoints(dbUtil db, long routeID, double latitude, double longitude) throws JSONException{
        db.insertPosition(String.valueOf(routeID), String.valueOf(latitude), String.valueOf(longitude));
    }

}
