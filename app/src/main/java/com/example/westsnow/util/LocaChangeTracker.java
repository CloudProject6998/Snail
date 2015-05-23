package com.example.westsnow.util;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;
/**
 * Created by yingtan on 5/18/15.
 */
public class LocaChangeTracker extends CurLocaTracker{

    private static final String TAG = "GpsActivity";
    private static final double DIST_DIFF_THRESHOLD = 0.0000000007; //0.000007
    private static final int DIST_INTERVAL = 100; // 1m:1000
    private static final int TIME_INTERVAL = 1; // 30s:30

    public static LatLng m_startLocation;
    public static LatLng m_endLocation;

    public Location m_LastLocation;
    public LocationManager m_manager;
    public static List<LatLng> m_routes = new ArrayList<LatLng>();


    public LocaChangeTracker(CurLocaTracker locaTracker){
        m_map = locaTracker.m_map;
        m_LastLocation = locaTracker.m_LastLocation;
        m_LastMarker = locaTracker.m_LastMarker;

        //m_startLocation = locaTracker.m_startLocation;
        //m_endLocation = locaTracker.m_endLocation;
        try{
            if ((m_map == null)) {
                throw new SnailException(SnailException.EX_DESP_MapNotExist);
            }
        } catch(SnailException e){
            System.out.println(SnailException.EX_DESP_MapNotExist);//??
        }
    }

    private LocationListener locationListener = new LocationListener(){

        public void onLocationChanged(Location location){
            try {
                if (location == null) {
                    throw new SnailException(SnailException.EX_DESP_LocationNotExist); // ??
                }
                LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
                m_routes.add(loc);
                if(m_endLocation != null){

                    System.out.println("[Listener Get End Pos]"+m_endLocation.latitude+" ,"+m_endLocation.longitude);
                    System.out.println("[Listener Get cur Pos]"+location.getLatitude()+" ,"+location.getLongitude());

                    if(ifReachDestination(location,m_endLocation)){
                        System.out.println("************** Reach Destination ! **************");
                        m_manager.removeGpsStatusListener(listener);
                        //m_manager.removeUpdates(locationListener);
                        return;
                    }
                }
                System.out.println("location changed!");

                LatLng lastLocation = new LatLng(m_LastLocation.getLatitude(),m_LastLocation.getLongitude());
                LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //display a point to move
                if ((m_LastMarker == null)) {
                    throw new NullPointerException();
                }
                m_LastMarker.remove();
                //m_LastMarker.setVisible(false);

                m_LastMarker = m_map.addMarker(new MarkerOptions()
                        .title("Current Location")
                        .snippet("The most populous city in")
                        .position(curLocation));

                //Todo 5: save it to DB


                Log.i(TAG, "changed longtitude:" + location.getLongitude());
                Log.i(TAG, "changed latitude:" + location.getLatitude());
                m_map.addPolyline(new PolylineOptions().add(lastLocation, curLocation).color(Color.BLUE).width(10));

                m_LastLocation = location;

            }catch(SnailException e){
                System.out.println(SnailException.EX_DESP_LocationNotExist);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("status changed!");
            switch (status) {
                case LocationProvider.AVAILABLE:
                    System.out.println("AVAILABLE!");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    System.out.println("OUT_OF_SERVICE!");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    System.out.println("TEMPORARILY_UNAVAILABLE!");
                    break;
            }
        }

        public void onProviderEnabled(String provider) {
            //Location location = lm.getLastKnownLocation(provider);
        }

        public void onProviderDisabled(String provider) {
        }
    };

    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    System.out.println("GPS_EVENT_FIRST_FIX!");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    System.out.println("GPS_EVENT_SATELLITE_STATUS!");

                    //test code
                    /*
                    //m_manager.removeGpsStatusListener(listener);
                    //return;
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()))
                            .radius(1000);
                            */
                    /*
                    LatLng lastLocation = new LatLng(m_LastLocation.getLatitude(),m_LastLocation.getLongitude());

                    double d3 = Math.random()*100;
                    System.out.println("add marker"+(m_LastLocation.getLatitude()+d3)+" ,"+(m_LastLocation.getLongitude()+d3));

                    double la = m_LastLocation.getLatitude()+d3;
                    double lg = m_LastLocation.getLongitude()+d3;
                    LatLng curLocation = new LatLng(la, lg);

                    m_map.addMarker(new MarkerOptions()
                            .title("Curr Location")
                            .snippet("The most populous city in")
                            .position(curLocation)
                            .draggable(true));

                */
                    //System.out.println("add circle");
                    //m_map.addCircle(circleOptions);

                    // extends route
                    /*
                    System.out.println("add line");
                    System.out.println(la + " " + lg);


                    m_map.addPolyline(new PolylineOptions().add(lastLocation,curLocation).color(Color.BLUE).width(10));

                */
                    //m_LastLocation.setLatitude(la);//your coords of course
                    //m_LastLocation.setLongitude(lg);

                    //System.out.println("endLocation:"+m_endLocation);
                    //if(m_endLocation != null){
                    //    System.out.println("[Listener GPS Get End Pos]"+m_endLocation.latitude+" ,"+m_endLocation.longitude);

                    //}

                /*
                    GpsStatus gpsStatus = m_manager.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    //System.out.println("count" +count);
                    break;
                    */
                case GpsStatus.GPS_EVENT_STARTED:
                    //System.out.println("GPS_EVENT_STARTED!");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    //System.out.println("GPS_EVENT_STOPPED!");
                    break;
            }
        };
    };

    public void trackChangedLocation(Context context){
        System.out.println("go into track!");
        m_manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if (!m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivityForResult(intent, 0);
            return;
        }
        //String bestProvider = lm.getBestProvider(getCriteria(), true);
        m_manager.addGpsStatusListener(listener);
        m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DIST_INTERVAL, TIME_INTERVAL, locationListener);

    }

    public Criteria getCriteria(){
        Criteria criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    public boolean ifReachDestination(Location curLocation, LatLng destLocation){
        double curLat = curLocation.getLatitude();
        double curLng = curLocation.getLongitude();

        double destLat = destLocation.latitude;
        double desLng = destLocation.longitude;

        double latDiff = Math.abs(curLat - destLat);
        double lngDiff = Math.abs(curLng - desLng);

        if((latDiff < DIST_DIFF_THRESHOLD) && (lngDiff < DIST_DIFF_THRESHOLD))
            return true;
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_manager.removeUpdates(locationListener);
    }


}
