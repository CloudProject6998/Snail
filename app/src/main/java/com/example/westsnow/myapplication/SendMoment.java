package com.example.westsnow.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.net.Uri;
import android.location.Location;

import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.ImageView;
import android.graphics.BitmapFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.File;


public class SendMoment extends ActionBarActivity {

    private String username;
    private EditText context;
    public double curLat;
    public double curLng;
    static final int RESULT_LOAD_IMG = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    String imgDecodableString;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private static final String URL = Constant.serverDNS + "/sendMoment.php";
    //    private static final String URL =  "http://ec2-52-24-240-104.us-west-2.compute.amazonaws.com/register.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private ImageView icon;
    public void send(View view){
        new SendMomentAttemp().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_moment);

        context=(EditText)findViewById(R.id.context);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        curLat = intent.getDoubleExtra("curlat", 0);
        curLng = intent.getDoubleExtra("curlng",0);


        icon = (ImageView) findViewById(R.id.open_image_from_disk_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //popup_menu();

                // custom dialog
                final Dialog dialog = new Dialog(SendMoment.this);
                //dialog.setContentView(R.layout.custom);
                dialog.setContentView(R.layout.gallery_popup);
                dialog.setTitle("Where to import the photo?");

                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.text);
                text.setText("Android custom dialog example!");

                Button dialogButtonLeft = (Button) dialog.findViewById(R.id.dialogButtonLeft);
                // if button is clicked, close the custom dialog
                dialogButtonLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadImagefromGallery();
                        dialog.dismiss();
                    }
                });
                Button dialogButtonRight = (Button) dialog.findViewById(R.id.dialogButtonRight);
                // if button is clicked, close the custom dialog
                dialogButtonRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(800, 400);
                //popup_alert();
            }

        });



    }//closing the setOnClickListener method

    public void popup_menu(){
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(SendMoment.this, icon);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.menu_send_moment, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.from_Local:
                            loadImagefromGallery();
                            return true;
                        case R.id.from_Camera:
                            dispatchTakePictureIntent();
                            return true;
                        default:
                            loadImagefromGallery();
                            return true;
                    }
                }
            });
            popup.show(); //showing popup menu
    }


    public void popup_alert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Your Title");

        // set dialog message
        alertDialogBuilder
                .setMessage("Where to import the photo?")
                .setCancelable(true)
                .setPositiveButton("Open Gallery",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        dialog.cancel();
                        loadImagefromGallery();
                    }
                })
                .setNegativeButton("Activate camera",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        dispatchTakePictureIntent();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }


    public void loadImagefromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.open_image_from_disk_icon);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                scaleImage();
            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ImageView imgView = (ImageView) findViewById(R.id.open_image_from_disk_icon);
                imgView.setImageBitmap(imageBitmap);
                scaleImage();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    private void scaleImage()
    {
        // Get the ImageView and its bitmap
        ImageView view = (ImageView) findViewById(R.id.open_image_from_disk_icon);
        Drawable drawing = view.getDrawable();
        if (drawing == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(500);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        view.setImageDrawable(result);
        Log.i("Test", "Apply the scaled bitmap");

        // Now change ImageView's dimensions to match the scaled image
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        Log.i("Test", "view.getLayoutParams()");
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
        Log.i("Test", "done");
    }

    private int dpToPx(int dp)
    {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
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

    public void cancel(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, PersonalPage.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    class SendMomentAttemp extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog *
         */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendMoment.this);
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

                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(URL, "GET", params);
                // checking log for json response
                Log.d("send moment attempt", json.toString());
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successfully registered", json.toString());
                    Intent ii = new Intent(SendMoment.this, PersonalPage.class);
//                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    ii.putExtra("username", username);
                    ii.putExtra("momentSent", "true");
                    ii.putExtra("curlat",curLat);
                    ii.putExtra("curlng",curLng);

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
                Toast.makeText(SendMoment.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}