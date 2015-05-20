package com.example.westsnow.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap;

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

import java.util.*;

import com.example.westsnow.myapplication.JSONParser;
import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;
import android.location.*;

/**
 * Created by yingtan on 5/19/15.
 */
public class MapUtil {
    private static MapUtil m_instance = new MapUtil();
    private static String API_KEY  = "AIzaSyCAu_Ff5LC0H17WPavjIhajVl7KXeck9mU";

    public static MapUtil getInstance() {
        if(m_instance == null)
            m_instance = new MapUtil();
        return m_instance;
    }

    private HttpResponse post(Map<String, Object> params, String url) {

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
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject getValues(Map<String, Object> params, String url) throws JSONException {
        String token = "";
        JSONObject obToken = null;
        HttpResponse response = post(params, url);
        if (response != null) {
            try {

                token = EntityUtils.toString(response.getEntity());
                obToken = new JSONObject(token);
                System.out.println("token route:"+token);
                response.removeHeaders("operator");
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obToken;
    }

    public List<List<LatLng>> getRoutes(String origin, String destination) throws JSONException {
        //String url ="https://maps.googleapis.com/maps/api/directions/json?origin=Queens&destination=Brooklyn&key="+API_KEY;

        String url ="https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+destination+"&mode=driving&key="+API_KEY;

        JSONObject obRoute =  getValues(null, url);
        return parseRoute(obRoute);
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

    private List<List<LatLng>> parseRoute(JSONObject jObject){

        //List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        List<List<LatLng>> routes = new ArrayList<List<LatLng>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");
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
                        List<LatLng> localist = decodePath(polyline, 10);

                        routes.add(localist);
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return routes;
    }

    private List<LatLng> decodePath(String encoded_polylines, int initial_capacity) {
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

            i = i + 2;
            j = j + 2;
        }
        return points;
    }

    public Polyline drawRoutes(List<List<LatLng>> routes, GoogleMap map){

        PolylineOptions polyLineOptions = new PolylineOptions();
        for(List<LatLng> route : routes){
            polyLineOptions.addAll(route);
            polyLineOptions.width(10);
            polyLineOptions.color(Color.RED);
        }
        LatLng startPos = null;
        if(routes.size() > 0){
            if(routes.get(0).size() > 0) {
                startPos = routes.get(0).get(0);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 13));
            }
        }



        return map.addPolyline(polyLineOptions);

    }

    public String formatInputLoca(String inputLoca){
        inputLoca =inputLoca.replace(" ","");

        return inputLoca;
    }
}
