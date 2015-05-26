package com.example.westsnow.myapplication;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] firstline;
    private final String[] secondline;
    private final Integer[] imgid;

    public CustomListAdapter(Activity context, String[] firstline, String[] secondline, Integer[] imgid) {
        //should not be used
        super(context, R.layout.grid_item, secondline);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.firstline=firstline;
        this.secondline=secondline;
        this.imgid=imgid;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.grid_item, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.secondLine);

        txtTitle.setText(firstline[position]);
        imageView.setImageResource(imgid[position]);
        extratxt.setText(secondline[position]);
        return rowView;

    };
}
