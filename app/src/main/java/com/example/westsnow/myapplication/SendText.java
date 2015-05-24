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
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SendText extends ActionBarActivity {

    private String username;
    private String routeID;
    private EditText context;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private static final String URL = Constant.serverDNS + "/sendText.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public void send(View view){
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_moment, menu);
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

    class SendMomentAttempt extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendText.this);
            pDialog.setMessage("Attempting for send moment...");
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
                params.add(new BasicNameValuePair("latitude", "0.0"));
                params.add(new BasicNameValuePair("longtitude", "0.0"));
                params.add(new BasicNameValuePair("routeId", "0.0"));


                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(URL, "GET", params);
                // checking log for json response
                Log.d("send moment attempt", json.toString());
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successfully registed!", json.toString());
                    Intent ii = new Intent(SendText.this, PersonalPage.class);
//                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    ii.putExtra("username", username);
                    ii.putExtra("routeID",routeID);
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
                Toast.makeText(SendText.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}