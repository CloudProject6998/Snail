package com.example.westsnow.util;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import java.net.HttpURLConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.example.westsnow.myapplication.HomePage;
import com.example.westsnow.myapplication.R;
import com.example.westsnow.myapplication.Constant;
import com.example.westsnow.myapplication.JSONParser;

import com.example.westsnow.myapplication.TimeLine;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap.*;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by yingtan on 5/19/15.
 */
public class CurLocaTracker extends ActionBarActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient m_GoogleApiClient;
    public static GoogleMap m_map = null;

    public Location m_LastLocation;
    public static Marker m_LastMarker;
    public static List<MarkerOptions> m_MomentMarkerOptions = new ArrayList<MarkerOptions>();

    public static LatLng m_startLocation;
    public static LatLng m_endLocation;

    protected String username;
    protected static long routeID;
    protected dbUtil db;


    public void buildGoogleApiClient() {
        m_GoogleApiClient = new GoogleApiClient.Builder(this) // after building, called onConnected (callback function) immediately
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() { // when load mapFragment, call onStart

        super.onStart();
        m_GoogleApiClient.connect();
        System.out.println("[Start !!!!]");
        /*
        MapUtil util = MapUtil.getInstance();
        if (LocaChangeTracker.m_trackerroutes.size() > 0) {
            System.out.println("[Start !!!!]" + LocaChangeTracker.m_trackerroutes);
            util.drawRoutes(LocaChangeTracker.m_trackerroutes, m_map, 2);
        } else
            System.out.println("[Start !!!!] null ; null");
            */
    }

    @Override
    protected void onResume() {
        System.out.println("[Resume !!!!]");
        super.onResume();
        m_GoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {// be triggered, when call connect()
        System.out.println("[Connected !!!!]");
        addCurMarker();
    }

    @Override
    protected void onPause() {

        System.out.println("[Paused !!!!]");
        super.onPause();
        if (m_GoogleApiClient.isConnected()) {
            m_GoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) { // when Map is loaded, call it
        System.out.println("Map is ready");
    }

    @Override
    protected void onStop() {
        System.out.println("[on stop !!!!]");
        super.onStop();
        if (m_GoogleApiClient.isConnected()) {
            m_GoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended");
        m_GoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void getCurLocation() {
        System.out.println("Connected, GetCurLocation ");
        try {
            m_LastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    m_GoogleApiClient);

            if (m_LastLocation == null) {
                System.out.println("Last location Null!!!!");
                throw new SnailException(SnailException.EX_DESP_LocationNotExist);
            }
        } catch (SnailException e) {
            System.out.println(SnailException.EX_DESP_LocationNotExist);
        }
    }

    protected synchronized void startTracker() { // called at click button
        LocaChangeTracker tracker = new LocaChangeTracker(this);
        tracker.trackChangedLocation(this);
    }

    public void addCurMarker() {
        getCurLocation();
        if (m_LastLocation != null) {
            LatLng curLocation = new LatLng(m_LastLocation.getLatitude(), m_LastLocation.getLongitude());
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13));

            //if (m_LastMarker != null)
            //    m_LastMarker.remove();

            //m_LastMarker = m_map.addMarker(new MarkerOptions()
            //        .title("Current Location")
            //        .snippet("Cur location")
            //        .position(curLocation)
            //        .alpha(0.9F)
            //        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
    }

    public void addMomentMarker(Location curLoca) {
        if (curLoca != null) {
            try {
                double lat = curLoca.getLatitude();
                double lng = curLoca.getLongitude();
                LatLng curLocation = new LatLng(lat, lng);

                m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13));

                int imageID = getResources().getIdentifier("snail", "drawable", getPackageName());

                JSONObject imgOb = dbUtil.getInstance().getImgUrl(username, lat, lng);
                String imgUrl = imgOb.getString("imgURL");
                String text = imgOb.getString("text");
                MarkerOptions lastMomentMarkerOption = new MarkerOptions()
                        .title(text) // title put : imgUrl
                        .snippet(imgUrl) //  snnipet put: text
                        .icon(BitmapDescriptorFactory.fromResource(imageID))
                        .position(curLocation);

                m_map.addMarker(lastMomentMarkerOption);
                m_MomentMarkerOptions.add(lastMomentMarkerOption);

                m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addExistedMarkers(int prevFlag){
        int len = m_MomentMarkerOptions.size();
        if(prevFlag == 1)
            len = len - 1;
        if(len-1 > 0){
            for(int i=0;i<len;i++){
                m_map.addMarker(m_MomentMarkerOptions.get(i));
            }
        }
        m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }

    public void addExistedMarkers(JSONArray markerJSONArray){
        try {
            int imageID = getResources().getIdentifier("snail", "drawable", getPackageName());
            for (int i = 0; i < markerJSONArray.length(); i++) {
                JSONObject obj = markerJSONArray.getJSONObject(i);
                double latitude = Double.parseDouble(obj.getString("latitude"));
                double longitude = Double.parseDouble(obj.getString("longitude"));
                String context = obj.getString("context");
                String imgURL = obj.getString("imageLocation");

                LatLng curLocation = new LatLng(latitude, longitude);
                MarkerOptions lastMomentMarkerOption = new MarkerOptions()
                        .title(context) // title put : imgUrl
                        .snippet(imgURL) //  snnipet put: text
                        .icon(BitmapDescriptorFactory.fromResource(imageID))
                        .position(curLocation);


                m_map.addMarker(lastMomentMarkerOption);
                m_MomentMarkerOptions.add(lastMomentMarkerOption);
                m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());
            }
        }catch(JSONException e){
            e.printStackTrace();;
        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View m_contentsView;

        MyInfoWindowAdapter() {
            m_contentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            // getting JSON string from URL
            dbUtil util = dbUtil.getInstance();
            try {
                String tvImgUrl = marker.getSnippet();
                String text = marker.getTitle();
                System.out.println("[set marker :imgurl" + tvImgUrl + " text" + text);
                TextView ivText = (TextView) m_contentsView.findViewById(R.id.title);
                ivText.setText(text);
                if(!tvImgUrl.equals("-1")) {
                    String imgUrl = Constant.serverDNS+"/"+tvImgUrl;

                    ImageView ivImage = ((ImageView) m_contentsView.findViewById(R.id.image));


                    Bitmap bitmap = new DownloadImageTask(ivImage).execute(imgUrl).get();
                    ivImage.setImageBitmap(bitmap);
                    ivImage.setClickable(true);
                    ivImage.getLayoutParams().height = 250;

                    m_map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker) {
                            Log.i("LinkTimeLine", "Image button is pressed, visible in LogCat");
                            Intent in = new Intent(getApplicationContext(),
                                    TimeLine.class);
                            // sending pid to next activity
                            in.putExtra("username", username);
                            in.putExtra("curlat", m_LastLocation.getLatitude());
                            in.putExtra("curlng", m_LastLocation.getLongitude());

                            // starting new activity
                            startActivity(in);
                        }
                    });
                }
                else{
                    ImageView ivImage = ((ImageView) m_contentsView.findViewById(R.id.image));
                    ivImage.setImageBitmap(null);
                    ivImage.getLayoutParams().height = 0;
                }


                return m_contentsView;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
    }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected  Bitmap doInBackground(String... urls) {

            String urldisplay = urls[0];
            Bitmap myBitmap = null;
            try {
                URL url = new URL(urldisplay);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                return bitmap;
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return myBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}