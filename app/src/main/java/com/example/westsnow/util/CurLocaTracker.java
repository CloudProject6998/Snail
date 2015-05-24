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
import android.content.res.*;
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

/**
 * Created by yingtan on 5/19/15.
 */
public class CurLocaTracker extends ActionBarActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    public GoogleApiClient m_GoogleApiClient;
    public static GoogleMap m_map = null;
    private final android.os.Handler handle = new Handler();

    public Location m_LastLocation;
    public static Marker m_LastMarker;
    public static Marker m_EndMarker;

    public static LatLng m_startLocation;
    public static LatLng m_endLocation;

    protected String username;
    protected static long routeID;
    protected dbUtil db;
    protected JSONObject m_json;

    public void buildGoogleApiClient(){
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

        MapUtil util = MapUtil.getInstance();
        if(LocaChangeTracker.m_trackerroutes.size() > 0){
            System.out.println("[Start !!!!]"  + LocaChangeTracker.m_trackerroutes);
            util.drawGoogleRoutes(LocaChangeTracker.m_trackerroutes,m_map,2);
        }
        else
            System.out.println("[Start !!!!] null ; null");
    }

    @Override
    protected void onResume() {
        System.out.println("[Resume !!!!]");
        super.onResume();
        m_GoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint){// be triggered, when call connect()
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
        System.out.println( "Connection suspended");
        m_GoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void getCurLocation(){
        System.out.println("Connected, GetCurLocation ");
        try {
            m_LastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    m_GoogleApiClient);

            if (m_LastLocation == null) {
                System.out.println("Last location Null!!!!");
                throw new SnailException(SnailException.EX_DESP_LocationNotExist);
            }
        }catch(SnailException e){
            System.out.println(SnailException.EX_DESP_LocationNotExist);
        }
    }

    protected synchronized void startTracker() { // called at click button
        LocaChangeTracker tracker = new LocaChangeTracker(this);
        tracker.trackChangedLocation(this);
    }

    public void addCurMarker(){
        getCurLocation();
        int imageID = getResources().getIdentifier("pin_2", "drawable", getPackageName());
        if(m_LastLocation != null) {
            LatLng curLocation = new LatLng(m_LastLocation.getLatitude(), m_LastLocation.getLongitude());
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13));

            if (m_LastMarker != null)
                m_LastMarker.remove();

            m_LastMarker = m_map.addMarker(new MarkerOptions()
                    .title("Current Location")
                    .snippet("Cur location")
                    .position(curLocation)
                    .alpha(0.9F)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
    }

    public void addMomentMarker(Location curLoca){
        if (curLoca != null) {

            LatLng curLocation = new LatLng(curLoca.getLatitude(), curLoca.getLongitude());

            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13));

            int imageID = getResources().getIdentifier("snail", "drawable", getPackageName());

            m_map.addMarker(new MarkerOptions()
                    .title("Moment Location")
                    .snippet("Moment location")
                            //.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .icon(BitmapDescriptorFactory.fromResource(imageID))
                    .position(curLocation));
            m_map.setInfoWindowAdapter(new MyInfoWindowAdapter());
        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private static final String TAG_USER = "users";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONParser jParser = new JSONParser();
        private final String url = Constant.serverDNS + "/getMoments.php";

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            // getting JSON string from URL
            params.add(new BasicNameValuePair("email", username));

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet()); //Should be changed to address on EC2
            //tvSnippet.setText(context); //Should be changed to address on EC2
            ImageView ivImage = ((ImageView)myContentsView.findViewById(R.id.image));
            //new DownloadImageTask(ivImage).execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
            ivImage.setImageResource(R.drawable.photoarea); //Should be changed to address on EC2
            ivImage.getLayoutParams().height = 250;
            //} catch (JSONException e) {
            //    e.printStackTrace();
            //}

            return myContentsView;
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

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    private Bitmap scaleImage(Resources res, int id, int lessSideSize) {
        Bitmap b = null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, id, o);

        float sc = 0.0f;
        int scale = 1;
        // if image height is greater than width
        if (o.outHeight > o.outWidth) {
            sc = o.outHeight / lessSideSize;
            scale = Math.round(sc);
        }
        // if image width is greater than height
        else {
            sc = o.outWidth / lessSideSize;
            scale = Math.round(sc);
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        b = BitmapFactory.decodeResource(res, id, o2);
        return b;
    }



}
