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

        public List<Long> recommendRoutes(double startlat, double startlong, double deslat, double deslong) throws JSONException, ExecutionException, InterruptedException{
            List<Long> routes = new ArrayList<Long>();
            dbUtil db = dbUtil.getInstance();
            double Dweight = 10^8;
            double Cweight = 0.2;
            double minone = Double.MAX_VALUE;
            double mintwo = Double.MAX_VALUE;
            double minthree = Double.MAX_VALUE;
            long one = 0;
            long two = 0;
            long three = 0;

            JSONArray StartEndPairs = db.getAllStartEnd();
            if (StartEndPairs != null) {
                Log.d("getStartEndPairs", StartEndPairs.toString());
                for (int i = 0; i < StartEndPairs.length(); i++) {
                    JSONObject c = StartEndPairs.getJSONObject(i);
                    String routeID = c.getString("routeID");
                    String userName = c.getString("userName");
                    double sLatt = Double.valueOf(c.getString("sLatt"));
                    double sLong = Double.valueOf(c.getString("sLong"));
                    double eLatt = Double.valueOf(c.getString("eLatt"));
                    double eLong = Double.valueOf(c.getString("eLong"));
                    int count = Integer.valueOf(c.getString("count"));
                    double distance = Math.sqrt(Math.pow(startlat - sLatt, 2) + Math.pow(startlong - sLong, 2)) + Math.sqrt(Math.pow(deslat - eLatt, 2) + Math.pow(deslong - eLong, 2));
                    double crt = distance * Dweight - count * Cweight;

                    if (crt < minone) {
                        minone = crt;
                        mintwo = minone;
                        minthree = mintwo;
                        one = Long.valueOf(routeID);
                        two = one;
                        three = two;
                    } else if (crt < mintwo) {
                        mintwo = crt;
                        minthree = mintwo;
                        two = Long.valueOf(routeID);
                        three = two;
                    } else if (crt < minthree) {
                        minthree = distance;
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
