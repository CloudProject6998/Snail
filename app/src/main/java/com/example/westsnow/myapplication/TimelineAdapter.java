package com.example.westsnow.myapplication;


import android.content.Context;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        System.out.println(n++);
        if (convertView == null) {
            inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.listview_item, null);
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
        viewHolder.likesNum.setTag(Integer.valueOf(position));

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
                viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
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




    static class ViewHolder {
        public TextView title;
        public TextView time;
        public ImageView iv;
        public String imageURL;
        public Bitmap bitmap;
        public TextView likesNum;
    }
}

