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
import android.app.ProgressDialog;

import com.example.westsnow.util.Route;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class PersonalPage extends CurLocaTracker {
    private String m_username;
    private String pageName = null;
    private final android.os.Handler handle = new Handler();
    public int m_drawLineType = 3; // type=1: draw google route , type =2: draw previous route , type =3: draw recommended routes

    public Location momentLoc ;
    public static String startLocName;
    public static String endLocName;
    public static boolean buttons_visible = false;
    public static boolean moment_clicable = false;
    public static boolean start_visible = true;
    public static boolean stop_visible = false;
    public static boolean upper_visible = true;


    public void SendPhoto() {
        Intent in = new Intent(getApplicationContext(),
                SendMoment.class);
        // sending pid to next activity
        in.putExtra("username", username);

        getCurLocation();
        momentLoc = m_LastLocation;
        in.putExtra("curlat",m_LastLocation.getLatitude());
        in.putExtra("curlng",m_LastLocation.getLongitude());
        in.putExtra("endLocName",endLocName);
        in.putExtra("startLocName",startLocName);
        Log.d("SendPhotoRouteID", String.valueOf(routeID));
        in.putExtra("routeID", String.valueOf(routeID));

        // starting new activity
        startActivity(in);
    }

    public void SendText() {
        Intent in = new Intent(getApplicationContext(),
                SendText.class);
        // sending pid to next activity
        getCurLocation();
        momentLoc = m_LastLocation;
        in.putExtra("username", username);
        in.putExtra("routeID",String.valueOf(routeID));
        in.putExtra("curlat", m_LastLocation.getLatitude());
        in.putExtra("curlng",m_LastLocation.getLongitude());

        in.putExtra("endLocName", endLocName);
        in.putExtra("startLocName", startLocName);
        // starting new activity
        startActivity(in);
    }

    public void PopSendMenu(View view) {
        if (moment_clicable == false) {
            System.out.println("** moment disenabled");
            Context context = getApplicationContext();
            CharSequence text = "Please start tracking before posting moments";
            int duration = Toast.LENGTH_SHORT;

            Toast.makeText(context,text,duration).show();
            return;
        }
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
        Log.d("trackRoute", "here");
        System.out.println("start tracker trackRoute");
        startTracker();
        //getCurLocation();

        db = dbUtil.getInstance();

        final EditText startText = (EditText)findViewById(R.id.start);
        final EditText endText = (EditText)findViewById(R.id.des);

        String startValue = startText.getText().toString();
        String endValue = endText.getText().toString();

        if (endValue.equals("")) {
            if(!endLocName.equals("")){
                endValue = endLocName;
            }
        }
        if (startValue.equals("")) {
            if(!startLocName.equals("")){
                startValue = startLocName;
            }
        }

        double[] startEndLocs = GeoCodeRequester.getInstance().getStartEndLocation(this, startValue, endValue, m_LastLocation);
        Route route = new Route();

        routeID = route.createNewRoute(db, username, startEndLocs[0], startEndLocs[1], startEndLocs[2], startEndLocs[3]);

        Log.d("getRouteID", String.valueOf(routeID));
        // set the moment button clickable
        View moment = findViewById(R.id.sendButton);
        if (moment_clicable == false) {
            moment.setBackgroundResource(R.drawable.shape_red);
            moment_clicable = true;
        }

        View stop = findViewById(R.id.stop);
        if (view.isShown()) {
            // set the start button invisible
            view.setVisibility(View.GONE);
            start_visible = false;
            // set the stop button visible
            stop.setVisibility(View.VISIBLE);
            stop_visible = true;
        }

        // hide the search bar
        View search = findViewById(R.id.UpLocation);
        upper_visible = false;
        search.setVisibility(View.GONE);
    }

    public void stopTrack(View view) {
        View start = findViewById(R.id.trackRoute);

        // set the stop button invisible
        view.setVisibility(View.GONE);
        stop_visible = false;

        // set the start button visible
        start.setVisibility(View.VISIBLE);
        start_visible = true;

        // set the moment button unclickable
        View moment = findViewById(R.id.sendButton);
        if (moment_clicable) {
            moment.setBackgroundResource(R.drawable.shape_red_trans);
            moment_clicable = false;
        }

        // show the search bar
        View search = findViewById(R.id.UpLocation);
        upper_visible = true;
        search.setVisibility(View.VISIBLE);

        // report to user
        Context context = getApplicationContext();
        CharSequence text = "Track Stopped!";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context,text,duration).show();

        // todo: stop tracking manually
        LocaChangeTracker.m_forceTrack = true;
        CurLocaTracker.m_endLocation = new LatLng(m_LastLocation.getLatitude(),m_LastLocation.getLongitude());
        dbUtil.getInstance().updateDes(CurLocaTracker.routeID + "", m_LastLocation.getLatitude() + "", m_LastLocation.getLongitude() + "");
    }

    public void GetRouteValue(View view) {
        // Update Location in time
        m_map.clear();
        MapUtil.clearStoredMarkerRoutes();

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
        endLocName = endValue;
        startLocName = startValue;

        final MapUtil util = MapUtil.getInstance();
        final String startPosName = util.formatInputLoca(startValue);
        final String endPosName = util.formatInputLoca(endValue);

        final Context context = this;

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(PersonalPage.this);
        pDialog.setMessage("loading recommended routes...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

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
                        util.drawRoutes(routes, m_map, m_drawLineType);
                    }
                });
                Route route = new Route();
                double[] startEndLocs = GeoCodeRequester.getInstance().getStartEndLocation(context, startPosName, endPosName, m_LastLocation);

                //db = dbUtil.getInstance();
                //MapUtil.storeUsefulRoutes(routes,db,startEndLocs, route, username); // /store useful routes

                List<Long> recommendedRoutes = route.recommendRoutes(startEndLocs[0], startEndLocs[1], startEndLocs[2], startEndLocs[3]);
                int count = 0;
                if (recommendedRoutes != null) {
                    for (int i = 0; i < recommendedRoutes.size(); i++) {
                        long routeId = recommendedRoutes.get(i);
                        final List<LatLng> routePoints = route.routePoints(routeId);
                        final JSONObject markerJsonOb  = dbUtil.getInstance().getMarkerList(routeId + "");
                        if (routePoints != null) {
                            handle.post(new Runnable() {
                                @Override
                                public void run() {
                                    m_drawLineType = 3;
                                    util.drawRoutes(routePoints, m_map, m_drawLineType);
                                    addExistedMarkers(markerJsonOb);
                                    addStartEndMarker(routePoints.get(routePoints.size() - 1), "e");
                                    addStartEndMarker(routePoints.get(0), "s");

                                }
                            });
                        }
                        count++;
                    }
                }
                System.out.println("count=" + count);

                if (count == 0) {
                    showToast("No routes recommended temporarily!");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        pDialog.dismiss();
                    }
                });
                GeoCodeRequester.getInstance().getStartEndLocation(context,startPosName,endPosName,m_LastLocation);
            }catch (ExecutionException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        pDialog.dismiss();
                    }
                });
            }catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismiss();
                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismiss();
                    }
                });
            }catch(SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_PathNotExist)) {
                    System.out.println("Path not exist");
                    showToast("No path exists! Please re-search!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                        }
                    });
                } else if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {
                    System.out.println("No internet");
                    showToast("No Internet! Please connect internet!");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                        }
                    });
                }
            }
            }
        }).start();
        if (!endValue.equals("")) {
        // set the buttons visible
            View b = findViewById(R.id.buttons);
            if (buttons_visible == false) {
                b.setVisibility(View.VISIBLE);
                buttons_visible = true;
            }
            // set the moment button unclickable
            View moment = findViewById(R.id.sendButton);
            if (moment_clicable == false) {
                moment.setBackgroundResource(R.drawable.shape_red_trans);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);

        View b = findViewById(R.id.buttons);
        View moment = findViewById(R.id.sendButton);
        View stop = findViewById(R.id.stop);
        View start = findViewById(R.id.trackRoute);
        View search = findViewById(R.id.UpLocation);

        // show the buttons' according to their visibility
        if (buttons_visible == false) {
            b.setVisibility(View.GONE);
        }else {
            b.setVisibility(View.VISIBLE);
        }

        // display the moment button's with its clickability
        if (moment_clicable == false) {
            moment.setBackgroundResource(R.drawable.shape_red_trans);
        }

        // show the stop or start button according to their visibility
        if (stop_visible == false) {
            stop.setVisibility(View.GONE);
        }

        if (start_visible == false) {
            start.setVisibility(View.GONE);
        }

        // show the search bar according to its visibility
        if (upper_visible == false) {
            search.setVisibility(View.GONE);
        }
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        super.username = intent.getStringExtra("username");
        System.out.println("[super's name]"+super.username);
        pageName = intent.getStringExtra("pageName");

        double lat = intent.getDoubleExtra("curlat", 0);
        double lng = intent.getDoubleExtra("curlng", 0);

        endLocName = intent.getStringExtra("endLocName");
        startLocName = intent.getStringExtra("startLocName");

        System.out.println("[user]" + username + "[page]" + pageName + "[lat]" + lat + "[lng]" + lng +"[endloc]"+ endLocName + "[startloc]" + startLocName);

        Location newCurLoca = new Location("");
        newCurLoca.setLongitude(lng);
        newCurLoca.setLatitude(lat);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        m_map = mapFragment.getMap();

        buildGoogleApiClient();
        int prevFlag = 0;
        if ((pageName != null)){
            if(pageName.equals("sendPhoto") || pageName.equals("sendText")) {
                prevFlag = 1; // just add previous points before current point
                addMomentMarker(newCurLoca); // add moment marker
            }
            addExistedMarkers(prevFlag);
            MapUtil.getInstance().drawExistedLines();
        }else {
            // from sign in/register, reset the buttons (set invisible)
            buttons_visible = false;
            b.setVisibility(View.GONE);

            moment_clicable = false;
            start_visible = true;
            stop_visible = false;

            // from sign in/register, reset the search bar (set visible)
            search.setVisibility(View.VISIBLE);
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
            getCurLocation();
            momentLoc = m_LastLocation;
            in.putExtra("username", username);
            in.putExtra("routeID",String.valueOf(routeID));
            in.putExtra("curlat",m_LastLocation.getLatitude());
            in.putExtra("curlng",m_LastLocation.getLongitude());
            in.putExtra("endLocName",endLocName);
            in.putExtra("startLocName",startLocName);
            // starting new activity
            startActivity(in);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PersonalPage.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
