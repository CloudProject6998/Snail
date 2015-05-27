package com.example.westsnow.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;

import com.example.westsnow.util.SnailException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SendText extends Activity {

    private String username;
    private String routeID;
    private EditText context;
    public double curLat;
    public double curLng;
    public String endLocName;
    public String startLocName;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private static final String URL = Constant.serverDNS + "/sendText.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public void send(){
        new SendMomentAttempt().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_text);

        context=(EditText)findViewById(R.id.context);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        routeID = intent.getStringExtra("routeID");
        curLat = intent.getDoubleExtra("curlat", 0);
        curLng = intent.getDoubleExtra("curlng", 0);
        startLocName = intent.getStringExtra("startLocName");
        endLocName = intent.getStringExtra("endLocName");

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_text, menu);
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
        } else if (id == R.id.sendTextBt) {
            Log.d("actionbar","here");
            new SendMomentAttempt().execute();
            return true;
        } else if (id == android.R.id.home) {
            Log.d("return","here");
            //NavUtils.navigateUpFromSameTask(this);
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("username", username);
            upIntent.putExtra("pageName","sendTextNull");
            upIntent.putExtra("curlat",curLat);
            upIntent.putExtra("curlng", curLng);
            upIntent.putExtra("routeID",routeID);
            upIntent.putExtra("endLocName", endLocName);
            upIntent.putExtra("startLocName", startLocName);
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SendMomentAttempt extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendText.this);
            pDialog.setMessage("Attempting for sending moment...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("userid", username));
                params.add(new BasicNameValuePair("context", context.getText().toString()));
                params.add(new BasicNameValuePair("latitude", String.valueOf(curLat)));
                params.add(new BasicNameValuePair("longtitude", String.valueOf(curLng)));
                params.add(new BasicNameValuePair("routeId", routeID));

                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(URL, "GET", params);
                if (json != null) {
                // checkin log for json response
                Log.d("send moment attempt", json.toString());
                 // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                } else {
                //throw new SnailException(SnailException.EX_DESP_JsonNull);
                    return "null";
                }
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successfully registed!", json.toString());
                    Intent ii = new Intent(SendText.this, PersonalPage.class);
//                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    ii.putExtra("username", username);
                    ii.putExtra("routeID",routeID);
                    ii.putExtra("pageName", "sendText");//Todo
                    ii.putExtra("curlat", curLat);
                    ii.putExtra("curlng", curLng);
                    ii.putExtra("endLocName", endLocName);
                    ii.putExtra("startLocName", startLocName);
                    startActivity(ii);
                    return json.getString(TAG_MESSAGE) + "~";
                } else {
                    return json.getString(TAG_MESSAGE) + "~";
                }
            }catch(SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {
                    showToast("No Internet! Please connect internet!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                        }
                    });
                }
            }catch (JSONException e) {
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
                    Toast.makeText(SendText.this, "Cannot connect to network!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SendText.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }

        public void showToast(final String toast) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SendText.this, toast, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}