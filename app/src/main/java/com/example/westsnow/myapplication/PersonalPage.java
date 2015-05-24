package com.example.westsnow.myapplication;

import com.example.westsnow.util.*;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.content.Context;
import android.widget.PopupMenu;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.example.westsnow.util.Route;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class PersonalPage extends CurLocaTracker {
    private String m_username;
    private String pageName = null;
    private final android.os.Handler handle = new Handler();
    public int m_drawLineType = 3; // type=1: draw google route , type =2: draw previous route , type =3: draw recommended routes

    public Location momentLoc ;

    public void SendPhoto() {
        Intent in = new Intent(getApplicationContext(),
                SendMoment.class);
        // sending pid to next activity
        in.putExtra("username", username);

        getCurLocation();
        momentLoc = m_LastLocation;
        in.putExtra("curlat",m_LastLocation.getLatitude());
        in.putExtra("curlng",m_LastLocation.getLongitude());
        Log.d("SendPhotoRouteID",String.valueOf(routeID));
        in.putExtra("routeID",String.valueOf(routeID));

        // starting new activity
        startActivity(in);
    }

    public void SendText() {
        Intent in = new Intent(getApplicationContext(),
                SendText.class);
        // sending pid to next activity
        in.putExtra("username", username);
        in.putExtra("routeID",String.valueOf(routeID));
        in.putExtra("curlat",m_LastLocation.getLatitude());
        in.putExtra("curlng",m_LastLocation.getLongitude());
        // starting new activity
        startActivity(in);
    }

    public void PopSendMenu(View view) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_send_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sendText:
                        SendText();
                        return true;
                    case R.id.sendPhoto:
                        SendPhoto();
                        return true;
                    case R.id.sendCancel:
                        return true;
                }
                return true;
            }
        });
        popup.show();
    }

    public void trackRoute(View view) throws JSONException, ExecutionException, InterruptedException{
        Log.d("trackRoute","here");
        System.out.println("start tracker trackRoute");
        startTracker();
        getCurLocation();

        db = dbUtil.getInstance();

        final EditText startText = (EditText)findViewById(R.id.start);
        final EditText endText = (EditText)findViewById(R.id.des);

        String startValue = startText.getText().toString();
        String endValue = endText.getText().toString();
        double[] startEndLocs = GeoCodeRequester.getInstance().getStartEndLocation(this, startValue, endValue, m_LastLocation);
        Route route = new Route();

        routeID = route.createNewRoute(db, username, startEndLocs[0], startEndLocs[1], startEndLocs[2], startEndLocs[3]);

        Log.d("getRouteID", String.valueOf(routeID));
        View b = findViewById(R.id.sendButton);
        b.setVisibility(View.VISIBLE);
    }

    public void GetRouteValue(View view) {

        // Update Location in time
        m_map.clear();
        getCurLocation();
        addCurMarker();

        final EditText startText = (EditText)findViewById(R.id.start);
        final EditText endText = (EditText)findViewById(R.id.des);

        String startValue = startText.getText().toString();
        String endValue = endText.getText().toString();
        if (endValue.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Please enter the start and end location";
            int duration = Toast.LENGTH_SHORT;

            Toast.makeText(context,text,duration).show();
        }else if(startValue.equals("")) {
            startValue = m_LastLocation.getLatitude()+","+m_LastLocation.getLongitude();
        }

        final MapUtil util = MapUtil.getInstance();
        final String startPosName = util.formatInputLoca(startValue);
        final String endPosName = util.formatInputLoca(endValue);

        final Context context = this;

        //When user press button, find route from start to end
        new Thread(new Runnable(){
            @Override
            public void run() {
            try {
                final List<LatLng> routes = util.getGoogleRoutes(startPosName, endPosName);
                if(routes == null){
                    throw new SnailException(SnailException.EX_DESP_NoInternet);
                }
                // get Googlemap's routes
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        m_drawLineType = 1;
                        util.drawGoogleRoutes(routes, m_map, m_drawLineType);
                    }
                });
                Route route = new Route();
                double[] startEndLocs = GeoCodeRequester.getInstance().getStartEndLocation(context,startPosName,endPosName,m_LastLocation);
                List<Long> recommendedRoutes = route.recommendRoutes(startEndLocs[0], startEndLocs[1], startEndLocs[2], startEndLocs[3]);

                if (recommendedRoutes != null) {
                    for (int i = 0; i < recommendedRoutes.size(); i++) {
                        final List<LatLng> routePoints = route.routePoints(recommendedRoutes.get(i));

                        if (routePoints != null) {
                            handle.post(new Runnable() {
                                @Override
                                public void run() {
                                    m_drawLineType = 3;
                                    util.drawGoogleRoutes(routePoints, m_map, m_drawLineType);
                                }
                            });
                        }
                    }
                }
                GeoCodeRequester.getInstance().getStartEndLocation(context,startPosName,endPosName,m_LastLocation);
            }catch (ExecutionException e) {
                e.printStackTrace();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }catch(JSONException e){
                e.printStackTrace();
            }catch(SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_PathNotExist)) {
                    System.out.println("Path not exist");
                    Looper.prepare();
                    Toast.makeText(PersonalPage.this, "No path exists! Please re-search!", Toast.LENGTH_LONG).show();
                    Looper.loop();
                } else if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {
                    System.out.println("No internet");
                    Looper.prepare();
                    Toast.makeText(PersonalPage.this, "No Internet! Please connect internet!", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
            }
        }).start();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);

        View b = findViewById(R.id.sendButton);
        b.setVisibility(View.GONE);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        pageName = intent.getStringExtra("pageName");

        double lat = intent.getDoubleExtra("curlat", 0);
        double lng = intent.getDoubleExtra("curlng", 0);

        System.out.println("[user]"+username+"[page]"+pageName+"[lat]"+lat+"[lng]"+lng);

        Location newCurLoca = new Location("");
        newCurLoca.setLongitude(lng);
        newCurLoca.setLatitude(lat);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        m_map = mapFragment.getMap();

        buildGoogleApiClient();
        if ((pageName != null)){
                System.out.println("[After send photo] !");
                addMomentMarker(newCurLoca); // add moment marker
                drawExistedLines();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.friendList) {
            Intent in = new Intent(getApplicationContext(),
                    HomePage.class);
            // sending pid to next activity
            in.putExtra("username", username);
            // starting new activity
            startActivity(in);
        }
        return super.onOptionsItemSelected(item);
    }

    public void drawExistedLines(){
        MapUtil util = MapUtil.getInstance();
        util.drawGoogleRoutes(MapUtil.m_googleRoutes, m_map, 1); // add google route searched before

        //add recommended route
        Route route = new Route();
        List<Long> prevRecomRoutes = Route.m_recomRoutes;
        try{
            if (prevRecomRoutes != null) {
                for (int i = 0; i < prevRecomRoutes.size(); i++) {
                    List<LatLng> routePoints = route.routePoints(prevRecomRoutes.get(i));
                    if (routePoints != null) {
                        util.drawGoogleRoutes(routePoints, m_map, 3);
                    }
                }
            }
        }catch (ExecutionException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
