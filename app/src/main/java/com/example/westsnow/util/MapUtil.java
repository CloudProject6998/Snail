package com.example.westsnow.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.example.westsnow.myapplication.PersonalPage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.*;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by yingtan on 5/19/15.
 */
public class MapUtil {
    private static MapUtil m_instance = new MapUtil();
    private static String API_KEY  = "AIzaSyCAu_Ff5LC0H17WPavjIhajVl7KXeck9mU";
    public static List<LatLng> m_googleRoutes = new ArrayList<LatLng>();

    public static MapUtil getInstance() {
        if(m_instance == null)
            m_instance = new MapUtil();
        return m_instance;
    }

    private HttpResponse post(Map<String, Object> params, String url) throws SnailException {

        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader("charset", HTTP.UTF_8);
        httpPost.setHeader("Content-Type",
                "application/x-www-form-urlencoded; charset=utf-8");

        HttpResponse response = null;
        if (params != null && params.size() > 0) {
            List<NameValuePair> nameValuepairs = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                nameValuepairs.add(new BasicNameValuePair(key, (String) params
                        .get(key)));
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuepairs,
                        HTTP.UTF_8));
                response = client.execute(httpPost);
                System.out.println("response:"+response);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response = client.execute(httpPost);
            } catch (ClientProtocolException e) { //Todo can not connect internet
                e.printStackTrace();
                throw new SnailException(SnailException.EX_DESP_NoInternet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject getValues(Map<String, Object> params, String url) throws JSONException, SnailException {
        String token = "";
        JSONObject obToken = null;
        try {
            HttpResponse response = post(params, url);
            if (response != null) {
                try {

                    token = EntityUtils.toString(response.getEntity());
                    obToken = new JSONObject(token);
                    System.out.println("token route:" + token);
                    response.removeHeaders("operator");
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch(SnailException e){
            throw e;
        }
        return obToken;
    }

    public List<LatLng> getGoogleRoutes(String origin, String destination) throws SnailException, JSONException {
        List<LatLng> routes = null;
        try {
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&mode=driving&key=" + API_KEY;

            JSONObject obRoute = getValues(null, url);
            if(obRoute == null)
                throw new SnailException(SnailException.EX_DESP_NoInternet); // /?//?/????
            routes = parseGoogleRoute(obRoute);
            if(routes != null)
                m_googleRoutes = routes;
        }
        catch(JSONException e){
            e.printStackTrace();

        }
        catch(SnailException e){
            throw e;
        }
        return routes;
    }
    /*
    public Object getAddress(String latlng) {
        String url = "https://maps.google.com/maps/api/geocode/json?latlng="+
                latlng+"&language=zh-CN&sensor=false";
        return getValues(null, url);
    }
    */
    /*
    public Object getLatlng(String str) {
        String url = "https://maps.google.com/maps/api/geocode/json?address="+
                str+"&language=zh-CN&sensor=false";
        return getValues(null, url);
    }
    */

    private List<LatLng> parseGoogleRoute(JSONObject jObject) throws SnailException{
        List<LatLng> routes = new ArrayList<LatLng>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        try {

            jRoutes = jObject.getJSONArray("routes");
            if((jRoutes == null) || (jRoutes.length() == 0))
                throw new SnailException(SnailException.EX_DESP_PathNotExist);

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs =((JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, Double>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        Object obStep = jSteps.get(k);
                        JSONObject jStep = (JSONObject)obStep;

                        String html_instructions = jStep.getString("html_instructions");
                        String travel_mode = jStep.getString("travel_mode");

                        String distance_text = jStep.getJSONObject("distance").getString("text");
                        String distance_value = jStep.getJSONObject("distance").getString("value");

                        String duration_text = jStep.getJSONObject("duration").getString("text");
                        String duration_value = jStep.getJSONObject("duration").getString("value");

                        String start_lat = jStep.getJSONObject("start_location").getString("lat");
                        String start_lon = jStep.getJSONObject("start_location").getString("lng");

                        String end_lat = jStep.getJSONObject("end_location").getString("lat");
                        String end_lon = jStep.getJSONObject("end_location").getString("lng");

                        String polyline = "";
                        polyline = (String)((JSONObject)(jStep).get("polyline")).get("points");
                        List<LatLng> localist = decodePath(polyline, 10,routes);
                    }
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }catch(SnailException e){
            throw e;
        }
        return routes;
    }

    private List<LatLng> decodePath(String encoded_polylines, int initial_capacity,List<LatLng> routes) {
        java.util.List<java.lang.Integer> trucks = new java.util.ArrayList<java.lang.Integer>(initial_capacity);

        List<LatLng> points = new ArrayList<LatLng>();
        int truck = 0;
        int carriage_q = 0;

        for (int x = 0, xx = encoded_polylines.length(); x < xx; ++x) {
            int i = encoded_polylines.charAt(x);
            i -= 63;
            int _5_bits = i << (32 - 5) >>> (32 - 5);
            truck |= _5_bits << carriage_q;
            carriage_q += 5;
            boolean is_last = (i & (1 << 5)) == 0;
            if (is_last) {
                boolean is_negative = (truck & 1) == 1;
                truck >>>= 1;
                if (is_negative) {
                    truck = ~truck;
                }
                trucks.add(truck);
                carriage_q = 0;
                truck = 0;
            }
        }
        int i=0;
        int j=1;
        int len = trucks.size();
        double prevLat = 0;
        double prevLng = 0;
        while((i < len) && (j < len)){
            prevLat = prevLat + trucks.get(i);
            prevLng = prevLng + trucks.get(j);

            double curLat = prevLat / 100000;
            double curLng = prevLng / 100000;
            LatLng curLocation = new LatLng(curLat,curLng);
            points.add(curLocation);
            routes.add(curLocation);

            i = i + 2;
            j = j + 2;
        }
        return points;
    }

    public void drawRoutes(List<LatLng> routes, GoogleMap map, int lineType) {

        if((routes == null)||(routes.size() == 0))
            return;

        LatLng endPos = routes.get(routes.size() - 1);

        PolylineOptions polyLineOptions = new PolylineOptions();
        PolylineOptions polyLineOptions_2 = new PolylineOptions();
        if (lineType == 1) { //draw google line : red
            int color_1 = Color.RED;
            for (LatLng route : routes) {
                polyLineOptions.add(route);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.WHITE);

                polyLineOptions_2.add(route);
                polyLineOptions_2.width(6);
                polyLineOptions_2.color(color_1);
            }
            map.addMarker(new MarkerOptions().title(PersonalPage.endLocName).position(endPos));
        }
        else if(lineType == 2){// draw previous line : blue
            int color_2 = Color.rgb(0, 152, 252);
            int color_board = Color.rgb(0, 102, 204);
            for (LatLng route : routes) {
                polyLineOptions.add(route);
                polyLineOptions.width(15);
                polyLineOptions.color(color_board);

                polyLineOptions_2.add(route);
                polyLineOptions_2.width(10);
                polyLineOptions_2.color(color_2);

            }
        } else if (lineType == 3){ //draw recommendation route
            //int color_3 = Color.rgb(255, 128, 0);
            int color_3 = Color.rgb(102,204,0);
            for (LatLng route : routes) {
                polyLineOptions.add(route);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.WHITE);

                polyLineOptions_2.add(route);
                polyLineOptions_2.width(6);
                polyLineOptions_2.color(color_3);
            }
        }
        map.addPolyline(polyLineOptions);
        map.addPolyline(polyLineOptions_2);
    }

    public void drawExistedLines(){
        MapUtil util = MapUtil.getInstance();
        GoogleMap map = CurLocaTracker.m_map;
        util.drawRoutes(MapUtil.m_googleRoutes, map, 1); // add google route searched before

        //add recommended route, and tracked routes
        Route route = new Route();
        List<Long> prevRecomRoutes = Route.m_recomRoutes;
        try{
            if (prevRecomRoutes != null) {
                for (int i = 0; i < prevRecomRoutes.size(); i++) {
                    List<LatLng> routePoints = route.routePoints(prevRecomRoutes.get(i));
                    if (routePoints != null) {
                        util.drawRoutes(routePoints, map, 3);
                    }
                }
            }
            if (LocaChangeTracker.m_trackerroutes.size() > 0) {
                System.out.println("[refill previous route !!!!]" + LocaChangeTracker.m_trackerroutes);
                util.drawRoutes(LocaChangeTracker.m_trackerroutes, map, 2);
            }
        }catch (ExecutionException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public String formatInputLoca(String inputLoca){
        inputLoca =inputLoca.replace(" ","");
        return inputLoca;
    }

    public static void clearStoredMarkerRoutes(){
        MapUtil.m_googleRoutes = new ArrayList<LatLng>();

        CurLocaTracker.m_MomentMarkerOptions = new ArrayList<MarkerOptions>();
        CurLocaTracker.m_LastMarker = null;
        //CurLocaTracker.m_startLocation = null;
        //CurLocaTracker.m_endLocation = null;

        //LocaChangeTracker.m_startLocation = null;
        //LocaChangeTracker.m_endLocation = null;
        LocaChangeTracker.m_trackerroutes = new ArrayList<LatLng>();
        LocaChangeTracker.m_forceTrack = true;
    }

    public static void storeUsefulRoutes(List<LatLng> routes, dbUtil db, double[] startEndLocs, Route route, String username){
        try {
            long routeID = route.createNewRoute(db, username, startEndLocs[0], startEndLocs[1], startEndLocs[2], startEndLocs[3]);
            for(LatLng r: routes){
                db.insertPosition(routeID+"", r.latitude+"", r.longitude+"");
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        catch(ExecutionException e){
            e.printStackTrace();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
