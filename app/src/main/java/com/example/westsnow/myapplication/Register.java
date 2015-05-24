package com.example.westsnow.myapplication;

import com.example.westsnow.util.*;
import com.google.android.gms.maps.model.LatLng;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Register extends ActionBarActivity {

    private EditText Username;
    private EditText Password;
    private EditText PasswordConfirm;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private static final String URL = Constant.serverDNS + "/register.php";
//    private static final String URL =  "http://ec2-52-24-240-104.us-west-2.compute.amazonaws.com/register.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public void register(View view){
        //try to register to server
        LocaChangeTracker.m_trackerroutes = new ArrayList<LatLng>();
        Route.m_recomRoutes = new ArrayList<Long>();
        MapUtil.m_googleRoutes = new ArrayList<LatLng>();

        new RegisterAttempt().execute();

//        if( registerSucceed() ) {
//            // go to homepage
//            Toast.makeText(Register.this, "Registration Succeed...", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(this, HomePage.class);
//            intent.putExtra("username", Username.getText().toString());
//            startActivity(intent);
//        }else{
//            Username.setText("");
//            Password.setText("");
//            Toast.makeText(Register.this, "Registration Failed...", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Username=(EditText)findViewById(R.id.username);
        Password=(EditText)findViewById(R.id.password);
        PasswordConfirm = (EditText)findViewById(R.id.passwordconfirm);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    class RegisterAttempt extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Register.this);
            pDialog.setMessage("Attempting for register...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String username = Username.getText().toString();
            String password = Password.getText().toString();
            String passwordConfirm = PasswordConfirm.getText().toString();

            if (!password.equals(passwordConfirm)) {
                return "pwConfirmError";
            }
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("email", username));
                params.add(new BasicNameValuePair("password", password));
                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(URL, "GET", params);
                if (json != null) {
                    // checking log for json response
                    Log.d("resigter attempt", json.toString());
                } else {
                //throw new SnailException(SnailException.EX_DESP_JsonNull);
                    return "null";
                }
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successfully registed!", json.toString());
                    Intent ii = new Intent(Register.this, PersonalPage.class);
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
                    Toast.makeText(Register.this, "Cannot connect to network!", Toast.LENGTH_LONG).show();
                } else if (message.equals("pwConfirmError")) {
                    Toast.makeText(Register.this, "Password Not Match! Please re-enter!", Toast.LENGTH_LONG).show();
                } else {
                        Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
