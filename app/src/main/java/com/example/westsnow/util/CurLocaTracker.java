package com.example.westsnow.util;

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


import com.example.westsnow.myapplication.R;
import com.example.westsnow.myapplication.Constant;
import com.example.westsnow.myapplication.JSONParser;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;

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
    private final JSONParser jParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";

    public Location m_LastLocation;
    public static Marker m_LastMarker;
    public static List<MarkerOptions> m_MomentMarkerOptions = new ArrayList<MarkerOptions>();

    public static LatLng m_startLocation;
    public static LatLng m_endLocation;

    protected String username;
    protected static long routeID;
    protected dbUtil db;
    protected JSONObject m_json;

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

            LatLng curLocation = new LatLng(curLoca.getLatitude(), curLoca.getLongitude());

            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13));

            int imageID = getResources().getIdentifier("snail", "drawable", getPackageName());

            MarkerOptions lastMomentMarkerOption =  new MarkerOptions()
                    .title("PhotoText")
                    .icon(BitmapDescriptorFactory.fromResource(imageID))
                    .position(curLocation);

            m_map.addMarker(lastMomentMarkerOption);
            m_MomentMarkerOptions.add(lastMomentMarkerOption);
            m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());
        }
    }

    public void addExistedMarkers(){
        int len = m_MomentMarkerOptions.size();
        if(len-1 > 0){
            for(int i=0;i<len-1;i++){
                m_map.addMarker(m_MomentMarkerOptions.get(i));
            }
        }
        m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            // getting JSON string from URL
            dbUtil util = dbUtil.getInstance();
            try {
                double lat = marker.getPosition().latitude;
                double lng = marker.getPosition().longitude;
                JSONObject imgOb = util.getImgUrl(username, lat, lng);
                System.out.println("[imgurl"+lat+""+lng+" "+imgOb.toString());
                String imgUrl = imgOb.getString("imgURL");

                String text = imgOb.getString("text");
                imgUrl = Constant.serverDNS+"/"+imgUrl;

                TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                tvTitle.setText(text);

                if(!imgUrl.equals("-1")) {
                    ImageView ivImage = ((ImageView) myContentsView.findViewById(R.id.image));

                    Bitmap bitmap = new DownloadImageTask(ivImage).execute(imgUrl).get();
                    ivImage.setImageBitmap(bitmap);
                    ivImage.getLayoutParams().height = 250;
                }
                return myContentsView;

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