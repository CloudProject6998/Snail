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
import android.widget.Toast;

import com.example.westsnow.myapplication.PersonalPage;
import com.example.westsnow.util.Route;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.*;
/**
 * Created by yingtan on 5/18/15.
 */
public class LocaChangeTracker extends CurLocaTracker{

    private static final String TAG = "GpsActivity";
    private static final double DIST_DIFF_THRESHOLD = 0.0008; //0.000007   0.0000000007
    private static final double RECORD_ROUTE_THRESHOLD = 0.00001; //0.000007   0.0000000007
    private static final int DIST_INTERVAL = 1000; // 1m:1000
    private static final int TIME_INTERVAL = 1; // 30s:30

    public static LatLng m_startLocation;
    public static LatLng m_endLocation;
    public static boolean m_forceTrack = false;


    public Location m_LastLocation;
    public LocationManager m_manager;
    public static List<LatLng> m_trackerroutes = new ArrayList<LatLng>();


    public LocaChangeTracker(CurLocaTracker locaTracker){
        m_map = locaTracker.m_map;
        m_LastLocation = locaTracker.m_LastLocation;
        m_LastMarker = locaTracker.m_LastMarker;

        m_startLocation = locaTracker.m_startLocation;
        m_endLocation = locaTracker.m_endLocation;

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

                LatLng lastLocation = new LatLng(m_LastLocation.getLatitude(),m_LastLocation.getLongitude());
                LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
                if(m_endLocation != null){
                    System.out.println("************** [Listener Get End Pos]"+m_endLocation.latitude+" ,"+m_endLocation.longitude);
                    System.out.println("************** [Listener Get cur Pos]"+location.getLatitude()+" ,"+location.getLongitude());
                    System.out.println("************** [Force Track]"+m_forceTrack);

                    int flagReach = ifReachDestination(location, m_endLocation,1);
                    int flagRecord = ifReachDestination(location,lastLocation,2);
                    if((flagReach == 1) ||(m_forceTrack)){
                        System.out.println("************** Reach Destination or force track! **************");
                        m_trackerroutes =  new ArrayList<LatLng>();
                        m_manager.removeGpsStatusListener(listener);
                        m_manager.removeUpdates(locationListener);

                        return;
                    }
                    else if(flagRecord == 2){
                        System.out.println("************** Add Route ! **************");
                        m_trackerroutes.add(loc); // enhance
                    }
                }
                System.out.println("location changed!");
                //display a point to move
                /*
                if ((m_LastMarker != null)) {
                    m_LastMarker.remove();
                }
                m_LastMarker = m_map.addMarker(new MarkerOptions()
                        .title("Current Location")
                        .snippet("The most populous city in")
                        .position(curLocation));
                        */

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Route route = new Route();

                Log.d("routeIDNullMe", String.valueOf(routeID));
                db = dbUtil.getInstance();
                route.addPoints(db, routeID, latitude, longitude);

                Log.i(TAG, "changed longtitude:" + location.getLongitude());
                Log.i(TAG, "changed latitude:" + location.getLatitude());

                //m_map.addPolyline(new PolylineOptions().add(lastLocation, curLocation).color(Color.rgb(0, 152, 252)).width(10));
                int color_2 = Color.rgb(0, 152, 252);
                int color_board = Color.rgb(0, 102, 204);
                m_map.addPolyline(new PolylineOptions().add(lastLocation, curLocation).color(color_board).width(15));
                m_map.addPolyline(new PolylineOptions().add(lastLocation,curLocation).color(color_2).width(10));

                m_LastLocation = location;

            }catch(SnailException e){
                System.out.println(SnailException.EX_DESP_LocationNotExist);
            }catch (JSONException e) {
                e.printStackTrace();
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
                    //System.out.println("************** [Force Track]"+m_forceTrack);
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
        String bestProvider = m_manager.getBestProvider(getCriteria(), true);
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

    public int ifReachDestination(Location curLocation, LatLng destLocation, int flag){
        double curLat = curLocation.getLatitude();
        double curLng = curLocation.getLongitude();

        double destLat = destLocation.latitude;
        double desLng = destLocation.longitude;

        double latDiff = Math.abs(curLat - destLat);
        double lngDiff = Math.abs(curLng - desLng);

        if(flag == 1) {
            double dis = Math.sqrt(latDiff * latDiff + lngDiff*lngDiff);
            System.out.println("************** [1. Listener Get Diff]" + latDiff + " ," + lngDiff+"  ,"+dis);

            if ((latDiff < DIST_DIFF_THRESHOLD) && (lngDiff < DIST_DIFF_THRESHOLD)) {
                return 1; // stop
            }
        }
        else if(flag == 2) {
            if ((latDiff >= RECORD_ROUTE_THRESHOLD) || (lngDiff >= RECORD_ROUTE_THRESHOLD)) { // keep record
                System.out.println("************** [2. Listener Get Diff]" + latDiff + " ," + lngDiff);
                return 2;
            }
        }

        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_manager.removeUpdates(locationListener);
    }




}
