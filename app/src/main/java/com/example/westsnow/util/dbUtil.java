package com.example.westsnow.util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.westsnow.myapplication.Constant;
import com.example.westsnow.myapplication.JSONParser;
import com.example.westsnow.myapplication.PersonalPage;
import com.google.android.gms.games.internal.api.SnapshotsImpl;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXNotRecognizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Created by xiaodiyue on 5/21/15.
 */

public class dbUtil {

    private final JSONParser jParser = new JSONParser();

    private static final String addPosURL = Constant.serverDNS + "/addpos.php";
    private static final String getPosURL = Constant.serverDNS + "/getroute.php";
    private static final String addStartEndURL = Constant.serverDNS + "/addStartEnd.php";
    private static final String getStartEndURL = Constant.serverDNS + "/getStartEnd.php";
    private static final String getImgURL = Constant.serverDNS + "/getImgUrl.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public String routeID = null;
    public String latitude = null;
    public String longitude = null;

    public String sLatt = null;
    public String sLong = null;
    public String eLatt = null;
    public String eLong = null;
    public String UserName = null;

    public static JSONArray positions = null;
    public static JSONArray StartEndPairs = null;
    public static String assignedRID = null;

    private static dbUtil m_instance = null;

    private dbUtil() {

    }

    public static dbUtil getInstance() {
        if (m_instance == null)
            m_instance = new dbUtil();

        return m_instance;
    }

    public void addComment(String mid, String text, String userId) {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(mid);
        passing.add(text);
        passing.add(userId);
        new addCommentByMid().execute(passing);
    }

    public JSONArray getCommentList(String mid) throws ExecutionException, InterruptedException {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(mid);
        return new LoadCommentByMid().execute(passing).get();

    }

    class LoadCommentByMid extends AsyncTask<ArrayList<String>, String, JSONArray> {

        protected JSONArray doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mid", passed.get(0)));
            String getMakerURL = Constant.serverDNS + "/getComment.php";
            try {
                JSONObject json = jParser.makeHttpRequest(getMakerURL, "GET", params);
                Log.d("GetCommentJson", json.toString());

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("success", "yes");
                    JSONArray comments = json.getJSONArray("comments");
                    return comments;
                } else {
                    Log.d("success", "no");
                    return null;
                }
            } catch (SnailException e) {


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    class addCommentByMid extends AsyncTask<ArrayList<String>, String, String> {
        @Override
        protected String doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mid", passed.get(0)));
                params.add(new BasicNameValuePair("text", passed.get(1)));
                params.add(new BasicNameValuePair("userId", passed.get(2)));
                String deleteURL = Constant.serverDNS + "/addComment.php";

                JSONObject json = jParser.makeHttpRequest(deleteURL, "GET", params);
                //Log.d("InsertStartEndAttempt:", json.toString());
                return json.getString("message");
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public void deleteMoment(String mid) {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(mid);
        new deleteMomentByMid().execute(passing);
    }

    class deleteMomentByMid extends AsyncTask<ArrayList<String>, String, String> {
        @Override
        protected String doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mid", passed.get(0)));
                String deleteURL = Constant.serverDNS + "/delete.php";

                JSONObject json = jParser.makeHttpRequest(deleteURL, "GET", params);
                //Log.d("InsertStartEndAttempt:", json.toString());
                return json.getString("message");
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void addLikes(String mid, String click) {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(mid);
        passing.add(click);
        new addLikesByMid().execute(passing);
    }

    class addLikesByMid extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mid", passed.get(0)));
                params.add(new BasicNameValuePair("click", passed.get(1)));
                String addLikesURL = Constant.serverDNS + "/addLikes.php";

                JSONObject json = jParser.makeHttpRequest(addLikesURL, "GET", params);
                //Log.d("InsertStartEndAttempt:", json.toString());
                return json.getString("message");
            } catch (SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public JSONObject getMarkerList(String routeID) throws ExecutionException, InterruptedException {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(routeID);
        return new LoadMakerByRoute().execute(passing).get();

    }

    class LoadMakerByRoute extends AsyncTask<ArrayList<String>, String, JSONObject> {

        protected JSONObject doInBackground(ArrayList<String>... passing) {
            //http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/getMarker.php?routeID=237
            /*
            {
                userName "",
                markerInfo: [
                    {
                        latitude: "40.8090246",
                        longitude: "-73.9592641",
                        context: "current ",
                        imageLocation: "image/09d8db600a7972ad7099c8317709e343"
                    }
                ],
                success: 1,
                message: "get maker info success"
            }
             */
            ArrayList<String> passed = passing[0];
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("routeID", passed.get(0)));
            String getMakerURL = Constant.serverDNS + "/getMarker.php";
            try {
                JSONObject json = jParser.makeHttpRequest(getMakerURL, "GET", params);

                Log.d("GetMakerHttp", json.toString());
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("success", "yes");
                    //JSONArray markerInfo = json.getJSONArray("markerInfo");
                    //Log.d("jsonArray",markerInfo.toString());
                    //if (markerInfo.length() == 0) {
                    //    return null;
                    //} else {
                    //    return markerInfo;
                    //}
                    return json;
                } else {
                    Log.d("success", "no");
                    return null;
                }
            } catch (SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    public JSONObject getImgUrl(String username, double latitude, double longitude) throws ExecutionException, InterruptedException {

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(username);
        passing.add(String.valueOf(latitude));
        passing.add(String.valueOf(longitude));
        JSONObject ret = new AttemptGetImgUrl().execute(passing).get();
        return ret;
    }

    class AttemptGetImgUrl extends AsyncTask<ArrayList<String>, String, JSONObject> {

        protected JSONObject doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", passed.get(0)));
            params.add(new BasicNameValuePair("latitude", passed.get(1)));
            params.add(new BasicNameValuePair("longitude", passed.get(2)));
            try {
                JSONObject json = jParser.makeHttpRequest(getImgURL, "GET", params);

                Log.d("GetImgJson", json.toString());

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("success", "yes");
                    JSONArray moments = json.getJSONArray("moments");
                    Log.d("jsonArray", moments.toString());
                    if (moments.length() == 0) {
                        return null;
                    } else {
                        JSONObject c = moments.getJSONObject(0);
                        //String context = c.getString("text");
                        //String imgURL = c.getString("imgURL");
                        return c;
                        //return ret.getString(0);
                    }
                } else {
                    Log.d("success whether", "no");
                    return null;
                }
            } catch (SnailException e) {
                if (e.getExDesp().equals(SnailException.EX_DESP_NoInternet)) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    /*
        http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/addpos.php?routeID=1&latt=40.809778&long=-73.961387
        {
            success: 1,
            message: "the location has been inserted."
        }
    */
    public void insertPosition(String routeID, String latitude, String longitude) throws JSONException {

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(routeID);
        passing.add(latitude);
        passing.add(longitude);
        new AttemptInsertPosition().execute(passing);
    }

    /*
        http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/getroute.php?routeID=1
        {
            positions: [ {latitude: "13.5",longitude: "42.7"}...],
            success: 1
        }
    */
    public JSONArray getRoute(String routeID) throws ExecutionException, InterruptedException {
        this.routeID = routeID;
        dbUtil.positions = new LoadALlPositions().execute().get();

        if (dbUtil.positions == null) {
            Log.d("getRouteAttemptStatus:", "null");
            return null;
        } else {
            //Log.d("get positions", dbUtil.positions.toString());
            return dbUtil.positions;
        }
    }


    public String insertStartEnd(String userName, String sLatitude, String sLongitude, String eLatitude, String eLongitude) throws ExecutionException, InterruptedException {

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(userName);
        passing.add(sLatitude);
        passing.add(sLongitude);
        passing.add(eLatitude);
        passing.add(eLongitude);
        dbUtil.assignedRID = new InsertStartEnd().execute(passing).get();

        Log.d("**insertStartEnd", String.valueOf(dbUtil.assignedRID));
        return dbUtil.assignedRID;
    }

    public JSONArray getAllStartEnd() throws ExecutionException, InterruptedException {
        dbUtil.StartEndPairs = new LoadALlStartEnd().execute().get();

        if (dbUtil.StartEndPairs == null) {
            Log.d("dbUtil status:", "null");
            return null;
        } else {
            Log.d("getStartEndPairs", dbUtil.StartEndPairs.toString());
            return dbUtil.StartEndPairs;
        }
    }

    class LoadALlStartEnd extends AsyncTask<String, String, JSONArray> {

        protected JSONArray doInBackground(String... args) {
            /*
                http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/getStartEnd.php
                {
                    StartEndPairs: [
                        {
                            routeID: "1",
                            userName: "diyue@gmail.com",
                            sLatt: "40.809578",
                            sLong: "-73.961387",
                            eLatt: "40.807373",
                            eLong: "-73.961312",
                            count: "0"
                        },etc
                    ],
                    succuss: 1
                }
            */
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            try {
                JSONObject json = jParser.makeHttpRequest(getStartEndURL, "GET", params);
                //Log.d("AllStartEnd: ", json.toString());

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    //Log.d("success whether","yes");
                    JSONArray ret = json.getJSONArray("StartEndPairs");
                    return ret;
                }
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONArray result) {
            if (result != null) {
                dbUtil.StartEndPairs = result;
                //Log.d("dbUtil onPost:", dbUtil.StartEndPairs.toString());
            }
        }

    }


    class InsertStartEnd extends AsyncTask<ArrayList<String>, String, String> {

        /*
    http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/addStartEnd.php?routeID=3&sLatt=40.209578
    &sLong=-73.961387&eLatt=40.807373&eLong=-73.961312&username=diyue@gmail.com
    {
        success: 1,
        message: "the data has been inserted."
    }
 */
        @Override
        protected String doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", passed.get(0)));
                params.add(new BasicNameValuePair("sLatt", passed.get(1)));
                params.add(new BasicNameValuePair("sLong", passed.get(2)));
                params.add(new BasicNameValuePair("eLatt", passed.get(3)));
                params.add(new BasicNameValuePair("eLong", passed.get(4)));

                JSONObject json = jParser.makeHttpRequest(addStartEndURL, "GET", params);
                Log.d("insertStartEnd", json.getString("message"));
                //Log.d("InsertStartEndAttempt:", json.toString());
                String getRouteID = json.getString("routeID");
                return getRouteID;
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String routeID) {
            dbUtil.assignedRID = routeID;
        }
    }


    class LoadALlPositions extends AsyncTask<String, String, JSONArray> {

        protected JSONArray doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            try {
                params.add(new BasicNameValuePair("routeID", routeID));
                JSONObject json = jParser.makeHttpRequest(getPosURL, "GET", params);
                //Log.d("RoutePositions: ", json.toString());

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    //Log.d("Enter here ", "right");
                    JSONArray ret = json.getJSONArray("positions");
                    return ret;
                }
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONArray result) {
            dbUtil.positions = result;
            //Log.d("dbUtil on post:", dbUtil.positions.toString());
        }

    }

    class AttemptInsertPosition extends AsyncTask<ArrayList<String>, String, String> {

        boolean failure = false;

        @Override
        protected String doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];
            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("routeID", passed.get(0)));
                params.add(new BasicNameValuePair("latt", passed.get(1)));
                params.add(new BasicNameValuePair("long", passed.get(2)));

                JSONObject json = jParser.makeHttpRequest(addPosURL, "GET", params);
                Log.d("InsertPosAttempt:", json.toString());

                return json.getString(TAG_MESSAGE) + "~";
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message) {
            if (message != null) {
                System.out.println(message);
            }
        }

    }


    public void updateDes(String routeID, String eLatt, String eLong) {
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(routeID);
        passing.add(eLatt);
        passing.add(eLong);
        new updateDesTask().execute(passing);
    }

    class updateDesTask extends AsyncTask<ArrayList<String>, String, String> {

        protected String doInBackground(ArrayList<String>... passing) {
            //http://ec2-52-24-19-59.us-west-2.compute.amazonaws.com/updateDes.php?routeID=247&eLatt=40.80948457&eLong=-73.96191687
            ArrayList<String> passed = passing[0];
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("routeID", passed.get(0)));
            params.add(new BasicNameValuePair("eLatt", passed.get(1)));
            params.add(new BasicNameValuePair("eLong", passed.get(2)));
            String getMakerURL = Constant.serverDNS + "/updateDes.php";
            String ret = null;
            try {
                JSONObject json = jParser.makeHttpRequest(getMakerURL, "GET", params);
                Log.d("GetUpdateJson", json.toString());
                ret = json.getString("message");
            } catch (SnailException e) {

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
        }
    }


}
