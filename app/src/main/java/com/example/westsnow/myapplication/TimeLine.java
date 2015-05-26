package com.example.westsnow.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.westsnow.util.dbUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TimeLine extends ListActivity {

    protected dbUtil db;
    ListView lv;
    String username;
    private ProgressDialog pDialog;
    ArrayList<String> momentList = new ArrayList<String>();
    JSONParser jParser = new JSONParser();
    private static final String url = Constant.serverDNS + "/getMoments.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "users";

    public double curLat;
    public double curLng;

    private TimelineAdapter timelineAdapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        curLat = intent.getDoubleExtra("curlat", 0);
        curLng = intent.getDoubleExtra("curlng", 0);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        lv = getListView();
        lv.setDividerHeight(0);
        new LoadAllMoments().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                //Log.d("click","here");
                TextView v = (TextView) view.findViewById(R.id.likesNum);
                String text =v.getText().toString();
                int num = Integer.valueOf(text);
                num += 1;
                v.setText(String.valueOf(num));

                String tag = v.getTag().toString();
                String mid = list.get(Integer.valueOf(tag)).get("mid").toString();
                Log.d("mid",mid);
                db = dbUtil.getInstance();
                db.addLikes(mid);
                //Log.d("tag",tag);
                //Log.d("text",text);

            }
        });
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
        } else if (id == android.R.id.home) {
            Log.d("timeline","here");
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("username", username);
            upIntent.putExtra("pageName","timeLine"); //Todo
            upIntent.putExtra("curlat",curLat);
            upIntent.putExtra("curlng", curLng);
            NavUtils.navigateUpTo(this, upIntent);

            return true;
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
                        String likes = c.getString("likes"); //diyue
                        String mid = c.getString("mid");
                        //momentList.add(time+ " | " + context);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("title", context);
                        map.put("time", time);
                        map.put("imageLocation", imageLocation );
                        map.put("likes",likes); //diyue
                        map.put("mid",mid);//diyue
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

                    lv = getListView();
                    LayoutInflater inflater = getLayoutInflater();
                    View header = inflater.inflate(R.layout.header, lv, false);
                    lv.addHeaderView(header, null, false);
                }
            });
        }
    }
}
