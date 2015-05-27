package com.example.westsnow.myapplication;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.westsnow.util.dbUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wumengting on 5/18/15.
 */
public class TimelineAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> list;
    private LayoutInflater inflater;

    private static int n=0;
    public TimelineAdapter(Context context, List<Map<String, Object>> list) {
        super();
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        System.out.printf("list.size()= %d\n", list.size());

        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        System.out.println(n++);
        if (convertView == null) {
            inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.listview_item, null);
            ImageView likesButton = (ImageView) convertView.findViewById(R.id.zan);
            likesButton.setClickable(true);
            likesButton.setFocusable(true);
            likesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View pv = (View) v.getParent();
                    TextView ltv = (TextView) pv.findViewById(R.id.likesNum);
                    String text = ltv.getText().toString();
                    Log.d("likeNum",text);
                    int num = Integer.valueOf(text);
                    String tag = ltv.getTag().toString();
                    try {
                        JSONObject obj = new JSONObject(tag);
                        int click = obj.getInt("click");
                        int pos = obj.getInt("pos");
                        String mid = list.get(Integer.valueOf(pos)).get("mid").toString();
                        String clickStr = String.valueOf(click);
                        Log.d("mid",mid);
                        Log.d("click",clickStr);
                        if (click == 0) {
                            num += 1;
                            ltv.setText(String.valueOf(num));
                            Map<String, Object> myMap = new HashMap<String, Object>();
                            myMap.put("click", 1);
                            myMap.put("pos", pos);
                            ltv.setTag(myMap);

                        } else {
                            num -= 1;
                            ltv.setText(String.valueOf(num));
                            Map<String, Object> myMap = new HashMap<String, Object>();
                            myMap.put("click", 0);
                            myMap.put("pos", pos);
                            ltv.setTag(myMap);
                        }
                        dbUtil db = dbUtil.getInstance();
                        db.addLikes(mid,clickStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

            //Add delete function
            ImageView deleteButton = (ImageView) convertView.findViewById(R.id.deleteBin);
            //hidden rubbish bin if necessary
            String flag = list.get(position).get("me").toString();
            if (flag.equals("0"))
                deleteButton.setVisibility(View.GONE);
            deleteButton.setClickable(true);
            deleteButton.setFocusable(true);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                private int pos = position;
                private String mid;
                @Override
                public void onClick(View v) {

                    View pv = (View) v.getParent();
                    mid = list.get(Integer.valueOf(pos)).get("mid").toString();
                    Log.d("binPos",String.valueOf(pos));
                    Log.d("bin","here");
                    Log.d("binMid",mid);
                    /*
                    list.remove(pos);
                    notifyDataSetChanged();
                    dbUtil db = dbUtil.getInstance();
                    db.deleteMoment(mid);
                    */

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Entry");
                    builder.setMessage("Are you sure to delete?");
                    builder.setPositiveButton("delete",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("ok","here");
                            list.remove(pos);
                            notifyDataSetChanged();
                            dbUtil db = dbUtil.getInstance();
                            db.deleteMoment(mid);
                        }
                    });
                    builder.setNegativeButton("cancel",new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("cancel","here");
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();


                }
            });




            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.time = (TextView) convertView.findViewById(R.id.show_time);
            viewHolder.iv = (ImageView) convertView.findViewById(R.id.myimage);
            viewHolder.likesNum = (TextView) convertView.findViewById(R.id.likesNum); //diyue


            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();


        String titleStr = list.get(position).get("title").toString();
        String timeStr = list.get(position).get("time").toString();
        String imageLocation = list.get(position).get("imageLocation").toString();
        String likes = list.get(position).get("likes").toString(); //diyue
        String mid = list.get(position).get("mid").toString();//diyue

        System.out.println(titleStr+"  "+imageLocation);


        viewHolder.title.setText(titleStr);
        viewHolder.time.setText(timeStr);
        viewHolder.imageURL=Constant.serverDNS +"/"+ imageLocation;
        viewHolder.likesNum.setText(likes); //diyue
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("click", 0);
        map.put("pos", position);
        //viewHolder.likesNum.setTag(Integer.valueOf(position));
        viewHolder.likesNum.setTag(map);

        if( !imageLocation.equals("-1"))
            new DownloadAsyncTask().execute(viewHolder);
        return convertView;
    }

    private class DownloadAsyncTask extends AsyncTask<ViewHolder, Void, ViewHolder> {

        @Override
        protected ViewHolder doInBackground(ViewHolder... params) {
            //load image directly
            ViewHolder viewHolder = params[0];
            try {
                URL imageURL = new URL(viewHolder.imageURL);
                System.out.println("viewHolder.imageURL:  "+viewHolder.imageURL);
                //viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());

                viewHolder.bitmap = decodeSampledBitmapFromStream(imageURL, viewHolder.title.getText(), 300, 300);
            } catch (IOException e) {
                Log.e("error",viewHolder.imageURL);
                Log.e("error", "Downloading Image Failed");
                viewHolder.bitmap = null;

            }

            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder result) {
            if (result.bitmap == null) {
                System.out.println("bitmap is null");
            } else {
                if(result.iv ==null)
                    System.out.println("image view is null");
                result.iv.setImageBitmap(result.bitmap);
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromStream(URL imageURL, CharSequence IDtext,
                                                       int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        try {
            HttpURLConnection connection = (HttpURLConnection) imageURL.openConnection();
            System.out.println("openConnection");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            System.out.println("options.inJustDecodeBounds = true;");

            InputStream is = new BufferedInputStream(connection.getInputStream());
            System.out.println("new BufferedInputStream(connection.getInputStream()):" + IDtext);

            BitmapFactory.decodeStream(is, null, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            System.out.println("options.inSampleSize: " + options.inSampleSize);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            connection = (HttpURLConnection) imageURL.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is, null, options);
            System.out.println("imgimg:  " + bitmap);
            is.close();

        } catch (IOException e) {
            System.out.printf("decodeSampledBitmapFromStream Failed: %s\n", IDtext);
        }
        return bitmap;

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //System.out.println("calculateInSampleSize_imageHeight:  "+height);
        //System.out.println("calculateInSampleSize_imageWidth:  "+width);
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        //System.out.println("calculateInSampleSize_inSampleSize:  "+inSampleSize);

        return inSampleSize;
    }


    static class ViewHolder {
        public TextView title;
        public TextView time;
        public ImageView iv;
        public String imageURL;
        public Bitmap bitmap;
        public TextView likesNum;
    }
}

