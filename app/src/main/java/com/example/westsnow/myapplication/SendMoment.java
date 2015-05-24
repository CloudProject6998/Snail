

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
import android.support.v4.app.NavUtils;
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
import android.app.Activity;

import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.ImageView;
import android.graphics.BitmapFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.File;


public class SendMoment extends Activity{

    private String username;
    private String routeID;
    private EditText context;
    static final int RESULT_LOAD_IMG = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
    String imgDecodableString;
    public double curLat;
    public double curLng;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    private static final String upLoadServerUri = Constant.serverDNS + "/uploadImage.php";

    //    private static final String URL =  "http://ec2-52-24-240-104.us-west-2.compute.amazonaws.com/register.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private ImageView icon;

    public void sendPhoto() {
        dialog = ProgressDialog.show(SendMoment.this, "", "Uploading file...", true);
        Thread t = new Thread(new Runnable() {
            public void run() {
                uploadFile(imgDecodableString);

            }
        });
        t.start();
        try {
            t.join();
            Intent intent = new Intent(this, PersonalPage.class);
            intent.putExtra("username", username);
            intent.putExtra("pageName", "sendPhoto");
            intent.putExtra("curlat",curLat);
            intent.putExtra("curlng",curLng);
            startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(View view){
        dialog = ProgressDialog.show(SendMoment.this, "", "Uploading file...", true);
        Thread t = new Thread(new Runnable() {
            public void run() {
                uploadFile(imgDecodableString);

            }
        });
        t.start();
        try {
            t.join();
            Intent intent = new Intent(this, PersonalPage.class);
            intent.putExtra("username", username);
            intent.putExtra("pageName", "sendPhoto");
            intent.putExtra("curlat",curLat);
            intent.putExtra("curlng",curLng);
            startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        new Thread(new Runnable() {
            public void run() {
                uploadFile(imgDecodableString);

            }
        }).start();*/
    }


    public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"+imgDecodableString);

//            runOnUiThread(new Runnable() {
//                public void run() {
//                    messageText.setText("Source File not exist :"+ imagepath);
//                }
//            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                java.net.URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);


                //add parameter userid
                dos.writeBytes("Content-Disposition: form-data; name=\"userid\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(username); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //add parameter context
                dos.writeBytes("Content-Disposition: form-data; name=\"context\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(context.getText().toString()); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //add parameter latitude
                dos.writeBytes("Content-Disposition: form-data; name=\"latitude\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("0.0"); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //add parameter longtitude
                dos.writeBytes("Content-Disposition: form-data; name=\"longtitude\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("0.0"); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //add parameter routeId
                dos.writeBytes("Content-Disposition: form-data; name=\"routeId\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(routeID); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);



                //upload file
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {
//                            messageText.setText("file uploaded");
                            Toast.makeText(SendMoment.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
//                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(SendMoment.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
//                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(SendMoment.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                    }
                });
//                Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_moment);

        context=(EditText)findViewById(R.id.context);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        routeID = intent.getStringExtra("routeID");
        curLat = intent.getDoubleExtra("curlat", 0);
        curLng = intent.getDoubleExtra("curlng",0);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("SendMomentGetRouteID",String.valueOf(routeID));


        icon = (ImageView) findViewById(R.id.open_image_from_disk_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //popup_menu();

                // custom dialog
                final Dialog dialog = new Dialog(SendMoment.this);
                dialog.setContentView(R.layout.custom);
                dialog.setTitle("Where to import the photo?");

//                // set the custom dialog components - text, image and button
//                TextView text = (TextView) dialog.findViewById(R.id.text);
//                text.setText("Android custom dialog example!");

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
        } else if (id == R.id.sendInMoment) {
            sendPhoto();
            return true;
        } else if (id == android.R.id.home) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra("username", username);
            upIntent.putExtra("pageName","sendPhotoNull"); //Todo
            upIntent.putExtra("curlat",curLat);
            upIntent.putExtra("curlng", curLng);
            NavUtils.navigateUpTo(this, upIntent);

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void cancel(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, PersonalPage.class);
        intent.putExtra("username", username);
        intent.putExtra("pageName","sendPhotoNull");
        intent.putExtra("curlat",curLat);
        intent.putExtra("curlng",curLng);
        startActivity(intent);
    }
}