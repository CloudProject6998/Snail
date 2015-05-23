package com.example.westsnow.myapplication;

import com.example.westsnow.util.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class PersonalPage extends CurLocaTracker {

//    class MyInfoWindowAdapter implements InfoWindowAdapter {
//
//        private final View myContentsView;
//
//        MyInfoWindowAdapter(){
//            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
//        }
//
//        @Override
//        public View getInfoContents(Marker marker) {
//
//            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
//            tvTitle.setText(marker.getTitle());
//            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
//            tvSnippet.setText(marker.getSnippet());
//
//            return myContentsView;
//        }
//
//        @Override
//        public View getInfoWindow(Marker marker) {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//    }

    private String momentSent;
    private final android.os.Handler handle = new Handler();
    private Polyline polyline = null;


    public void homepage(View view){
        Intent in = new Intent(getApplicationContext(),
                HomePage.class);
        // sending pid to next activity
        in.putExtra("username", username);
        // starting new activity
        startActivity(in);
    }

    public void sendMoment(View view){
        Intent in = new Intent(getApplicationContext(),
                SendMoment.class);
        // sending pid to next activity
        in.putExtra("username", username);
        // starting new activity
        startActivity(in);
    }

    public void PopSendMenu(View view) {
        Intent in = new Intent(getApplicationContext(),
                SendMoment.class);
        // sending pid to next activity
        in.putExtra("username", username);
        // starting new activity
        startActivity(in);

    }

    public void GetRouteValue(View view) {

        final EditText startText = (EditText)findViewById(R.id.start);
        final EditText endText = (EditText)findViewById(R.id.des);

        String startValue = startText.getText().toString();
        String endValue = endText.getText().toString();
        if (startValue.equals("") || endValue.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Please enter the start and end location";
            int duration = Toast.LENGTH_SHORT;

            Toast.makeText(context,text,duration).show();
        }
        System.out.println("start is:" + startValue);
        System.out.println("end is:" + endValue);

        final MapUtil util = MapUtil.getInstance();
        final String startPosName = util.formatInputLoca(startValue);
        final String endPosName = util.formatInputLoca(endValue);

        //When user press button, find route from start to end
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {

                    final List<List<LatLng>> routes = util.getRoutes(startPosName, endPosName);
                    handle.post(new Runnable() {
                        @Override
                        public void run() {
                            if(polyline != null)
                                polyline.remove();
                            polyline  = util.drawRoutes(routes, m_map);
                        }
                    });
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(SnailException e){
                    if(e.getExDesp().equals(SnailException.EX_DESP_PathNotExist)){
                        System.out.println("Path not exist");
                        // Todo : add dialogue to show No Path Found
                        Looper.prepare();
                        Toast.makeText(PersonalPage.this, "No path exists! Please re-search!", Toast.LENGTH_LONG).show();
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

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        momentSent = intent.getStringExtra("momentSent");

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        m_map = mapFragment.getMap();

        //1) Show Map  2) Get Cur Loca 3) Update Location in time
        buildGoogleApiClient();

        //3) GeoCoding
        // Todo: after clicking button, get requested locationName, and request latitude, longtitude
        /*
        String locationName = "600 Independence Ave SW, Washington, DC 20560";
        GeoCodeRequester codeRequester = GeoCodeRequester.getInstance();
        codeRequester.getGeoLocation(this,locationName);
        */



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

    public void testDB(View view) throws JSONException, ExecutionException, InterruptedException {
        dbUtil db = new dbUtil();


        // Func: insert (routeID, latitude, longitude) pairs to db.
        db.insertPosition("1","12.3","45.6");
        db.insertPosition("1","12.345","45.678");




        // Func: get all (latitude, longitude) pairs by routeID.
        JSONArray posPairs = db.getRoute("1");
        if (posPairs != null) {
            Log.d("getPositions", posPairs.toString());
            for (int i = 0; i < posPairs.length(); i++) {
                JSONObject c = posPairs.getJSONObject(i);
                String latitude = c.getString("latitude");
                String longitude = c.getString("longitude");
                Log.d("latt", latitude);
                Log.d("long", longitude);
            }
        } else {
                Log.d("getPositions","null");
            }




        // Func: insert (routeID, userID, sLatt, sLong, eLatt, eLong) to db.
        db.insertStartEnd("diyue@gmail.com","1","1","1","1");
        db.insertStartEnd("diyue@gmail.com","13","13","13","13");





        // Func: get all (routeID, userName, start, end ) tuples from db.
        JSONArray StartEndPairs = db.getAllStartEnd();;
        if (StartEndPairs != null) {
            Log.d("getStartEndPairs",StartEndPairs.toString());
            for (int i = 0; i <StartEndPairs.length(); i++) {
                JSONObject c = StartEndPairs.getJSONObject(i);
                String routeID = c.getString("routeID");
                String userName = c.getString("userName");
                String slatitude = c.getString("sLatt");
                String slongitude = c.getString("sLong");
                String elatitude = c.getString("eLatt");
                String elongitude = c.getString("eLong");

                Log.d("routeID", routeID);
                Log.d("username", userName);
                Log.d("sLatt", slatitude);
                Log.d("sLong", slongitude);
                Log.d("eLatt", elatitude);
                Log.d("eLong", elongitude);
            }
        } else {
            Log.d("getStartEndPairs","null");
        }


    }


}
