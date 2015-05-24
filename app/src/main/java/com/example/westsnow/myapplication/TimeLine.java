package com.example.westsnow.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TimeLine extends ListActivity {

    ListView lv;
    String username;
    private ProgressDialog pDialog;
    ArrayList<String> momentList = new ArrayList<String>();
    JSONParser jParser = new JSONParser();
    private static final String url = Constant.serverDNS + "/getMoments.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "users";

    private TimelineAdapter timelineAdapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        lv = getListView();
        lv.setDividerHeight(0);
        new LoadAllMoments().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time_line, menu);
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
        } else if (id == R.id.home) {
            Intent intent = new Intent(this, PersonalPage.class);
            intent.putExtra("username", username);
            intent.putExtra("pageName","sendPhoto");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadAllMoments extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TimeLine.this);
            pDialog.setMessage("loading moments...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", username));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All moments: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    JSONArray moments = json.getJSONArray(TAG_USER);

                    // looping through All Products
                    for (int i = moments.length()-1; i >= 0; i--) {
                        JSONObject c = moments.getJSONObject(i);
                        // Storing each json item in variable
                        String context = c.getString("context");
                        String time = c.getString("time");
                        String imageLocation = c.getString("imageLocation");
                        //momentList.add(time+ " | " + context);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("title", context);
                        map.put("time", time);
                        map.put("imageLocation", imageLocation );
                        list.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    timelineAdapter = new TimelineAdapter(TimeLine.this, list);
                    lv.setAdapter(timelineAdapter);
                }
            });
        }
    }
}
