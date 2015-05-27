package com.example.westsnow.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import android.widget.SimpleAdapter;

import com.example.westsnow.util.CurLocaTracker;
import com.example.westsnow.util.MapUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


//show friend list
public class HomePage extends ListActivity {

    ListView lv;
    String username;
    private ProgressDialog pDialog;
    ArrayList<String> following = new ArrayList<String>();
    JSONParser jParser = new JSONParser();
    private static final String url = Constant.serverDNS + "/getFollowings.php";
    private static final String addFriendURL = Constant.serverDNS + "/addFriend.php";

//    private static final String url = "http://ec2-52-24-240-104.us-west-2.compute.amazonaws.com/getFollowings.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "users";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_MESSAGE = "message";

    private EditText targetEmail;

    public double curLat;
    public double curLng;
    public String endLocName;
    public String startLocName;

    //add funtion of addFriend Button
    public void addFriend(View view){
        new AddFriend().execute();
    }

    String[] itemname ={
            "My Profile",
            "Oguri Shun",
            "Sharon",
            "Roger Federer",
            "Dean",
            "Catherina",
            "Belinda",
            "Ian"
    };

    Integer[] imgid={
            R.drawable.contact1,
            R.drawable.contact2,
            R.drawable.contact3,
            R.drawable.contact4,
            R.drawable.contact5,
            R.drawable.contact6,
            R.drawable.contact7,
            R.drawable.contact8,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        targetEmail = (EditText) findViewById(R.id.email);
        Intent intent = getIntent();
        username  = intent.getStringExtra("username");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        curLat = intent.getDoubleExtra("curlat",0);
        curLng = intent.getDoubleExtra("curlng",0);
        startLocName = intent.getStringExtra("startLocName");
        endLocName = intent.getStringExtra("endLocName");


//        // create the grid item mapping
//        String[] from = new String[] {"icon", "Alias", "Description"};
//        int[] to = new int[] { R.id.icon, R.id.firstLine, R.id.secondLine};
//        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
//        for(int i = 0; i < 10; i++){
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.put("icon", Integer.toString(R.drawable.contact_icon));
//            map.put("Alias", "Unknown");
//            map.put("Description", "#$%^&@163.com");
//            fillMaps.add(map);
//        }
//        //SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
//        CustomListAdapter adapter=new CustomListAdapter(this, itemname, itemname, imgid);
//        lv = getListView();
//        lv.setAdapter(adapter);

        lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        new LoadALlFriends().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                Log.d("click","here");
                String selectedUser = ((TextView) view.findViewById(R.id.secondLine)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        TimeLine.class);
                // sending pid to next activity
                in.putExtra("selectedUser", selectedUser);
                in.putExtra("username",username);
                in.putExtra("curlat",curLat);
                in.putExtra("curlng",curLng);
                in.putExtra("endLocName", endLocName);
                in.putExtra("startLocName", startLocName);


                // starting new activity
                startActivity(in);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return super.onCreateOptionsMenu(menu);
        //return true;
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
            Log.d("home","here");
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("username", username);
            upIntent.putExtra("pageName", "friendList"); //Todo
            upIntent.putExtra("curlat",curLat);
            upIntent.putExtra("curlng", curLng);
            upIntent.putExtra("endLocName", endLocName);
            upIntent.putExtra("startLocName", startLocName);
            NavUtils.navigateUpTo(this,upIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadALlFriends extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomePage.this);
            pDialog.setMessage("loading friends...");
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

           if (json != null) {
                // Check your log cat for JSON reponse
                Log.d("All friends: ", json.toString());
            } else {
                //throw new SnailException(SnailException.EX_DESP_JsonNull);
                return "null";
            }

            following.add(username);
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    JSONArray friends = json.getJSONArray(TAG_USER);

                    // looping through All Products
                    for (int i = 1; i < friends.length(); i++) {
                        JSONObject c = friends.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_EMAIL);
                        following.add(id);
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
            if (file_url != null) {
                if (file_url.equals("null")) {
                    Toast.makeText(HomePage.this, "Cannot connect to network!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomePage.this, file_url, Toast.LENGTH_LONG).show();
                }
            } else {

                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        ArrayAdapter<String> codeLearnArrayAdapter =
                                new ArrayAdapter<String>(HomePage.this, R.layout.grid_item, R.id.secondLine, following);
                        String[] followingArr = new String[following.size()];
                        CustomListAdapter customAdapter = new CustomListAdapter(HomePage.this, itemname, following.toArray(followingArr), imgid);

                        //lv.setAdapter(codeLearnArrayAdapter);
                        lv.setAdapter(customAdapter);
//                    /**
//                     * Updating parsed JSON data into ListView
//                     * */
//                    ListAdapter adapter = new SimpleAdapter(
//                            AllProductsActivity.this, productsList,
//                            R.layout.list_item, new String[] { TAG_PID,
//                            TAG_NAME},
//                            new int[] { R.id.pid, R.id.name });
//                    // updating listview
//                    setListAdapter(adapter);

                    }
                });
            }
        }

    }


    class AddFriend extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomePage.this);
            pDialog.setMessage("Attempting for adding new Friend...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String email = targetEmail.getText().toString();
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("following", email));
                params.add(new BasicNameValuePair("follower", username));
                Log.d("friend request!", "starting");
                JSONObject json = jParser.makeHttpRequest(addFriendURL, "GET", params);
                if (json != null) {
                    // checking log for json response
                    Log.d("friend request attempt", json.toString());
                 }else {
                        return "null";
                }
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("add friend succeed", json.toString());
                    //Intent it = new Intent(HomePage.this, HomePage.class);
                    //startActivity(it);
                    return json.getString(TAG_MESSAGE) + "~";
                } else {
                    Log.d("add friend failed", json.toString());
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
                Toast.makeText(HomePage.this, message, Toast.LENGTH_LONG).show();
                if (message.equals("null")) {
                    Toast.makeText(HomePage.this, "Cannot connect to network!", Toast.LENGTH_LONG).show();
                } else {
                Toast.makeText(HomePage.this, message, Toast.LENGTH_LONG).show();
            }
          }
        }

    }
}
