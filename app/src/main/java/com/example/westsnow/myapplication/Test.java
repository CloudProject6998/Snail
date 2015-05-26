package com.example.westsnow.myapplication;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Time;


public class Test extends ListActivity {

    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        lv = getListView();

        String[] codeLearnChapters = new String[] { "Android Introduction","Android Setup/Installation","Android Hello World",
                "Android Layouts/Viewgroups","Android Activity & Lifecycle","Intents in Android"};

        ArrayAdapter<String> codeLearnArrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, codeLearnChapters);

        lv.setAdapter(codeLearnArrayAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String username = ((TextView) view).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        TimeLine.class);
                // sending pid to next activity
                in.putExtra("username", username);

                // starting new activity
                startActivity(in);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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
}
