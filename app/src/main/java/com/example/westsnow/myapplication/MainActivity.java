package com.example.westsnow.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Looper;

import com.example.westsnow.util.CurLocaTracker;
import com.example.westsnow.util.LocaChangeTracker;
import com.example.westsnow.util.MapUtil;
import com.example.westsnow.util.Route;
import com.example.westsnow.util.SnailException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity {

    private Button signIn;
    private Button signUp;
    private EditText Username;
    private EditText Password;
    // Progress Dialog
    private ProgressDialog pDialog;
    //JSON parser class
    JSONParser jsonParser = new JSONParser();

    private static final String LOGIN_URL = Constant.serverDNS + "/login.php";
//    private static final String LOGIN_URL = "http://ec2-52-24-240-104.us-west-2.compute.amazonaws.com/login.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private boolean signInSucceed() {
        String username = Username.getText().toString();
        String passwd = Password.getText().toString();
        if (username.equals("shuai") && passwd.equals("123"))
            return true;
        return false;
    }

    public void register(View view) {
        //go to register activity
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    public void login(View view) {

        LocaChangeTracker.m_trackerroutes = new ArrayList<LatLng>();
        Route.m_recomRoutes = new ArrayList<Long>();
        MapUtil.m_googleRoutes = new ArrayList<LatLng>();

        new AttemptLogin().execute();

//        if (signInSucceed()) {
//            Toast.makeText(MainActivity.this, "Login Successful ...", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(this, HomePage.class);
//            intent.putExtra("username", Username.getText().toString());
//            startActivity(intent);
//        } else {
//            Username.setText("");
//            Password.setText("");
//            Toast.makeText(MainActivity.this, "Authentication Failed...", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = (Button) findViewById(R.id.signInButton);
        signUp = (Button) findViewById(R.id.signUpButton);
        Username = (EditText) findViewById(R.id.username);
        Password = (EditText) findViewById(R.id.password);

        MapUtil.clearStoredMarkerRoutes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    class AttemptLogin extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Attempting for login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args){
            int success;
            String username = Username.getText().toString();
            String password = Password.getText().toString();
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("email", username));
                params.add(new BasicNameValuePair("password", password));
                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "GET", params);
                System.out.println("JSON received from login in username" + json);
                if (json != null) {
                    // checking log for json response
                    Log.d("Login attempt", json.toString());
                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                } else {
                    //throw new SnailException(SnailException.EX_DESP_JsonNull);
                    return "null";

                }
                if (success == 1) {
                    Log.d("Successfully Login!", json.toString());
                    Intent ii = new Intent(MainActivity.this, PersonalPage.class);
//                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    ii.putExtra("username", username);
                    startActivity(ii);
                    return json.getString(TAG_MESSAGE) + "~";
                } else {
                    return json.getString(TAG_MESSAGE) + "~";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Once the background process is done we need to Dismiss the progress dialog asap * *
         */
        protected void onPostExecute(String message) {
            pDialog.dismiss();
            if (message != null) {
                if (message.equals("null")) {
                    Toast.makeText(MainActivity.this, "Cannot connect to network!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}



