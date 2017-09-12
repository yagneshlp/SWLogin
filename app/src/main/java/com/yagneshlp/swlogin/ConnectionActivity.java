package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class ConnectionActivity extends Activity {

    boolean resp = false;
    SessionManager session;
    private static final String TAG = ConnectionActivity.class.getSimpleName(); //for Logger Purposes
    ProgressDialog progress ;

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        progress.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting ... ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        session = new SessionManager(getApplicationContext());
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {

                Log.d(TAG, "Checking if internet connection exists");

                    String tag_string_req = "req_page1_Sub";
                    StringRequest strReq = new StringRequest(Request.Method.POST, "http://swlogin.yagneshlp.com/pinp.php"
                            , new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Ping Response: " + response.toString());

                            try {
                                JSONObject jObj = new JSONObject(response); //objectifying the json
                                boolean error = jObj.getBoolean("error");  //detecting if an error was sent in json
                                // Check for error node in json
                                if (!error) {
                                   // Toast.makeText(getApplicationContext(), "Internet Access Exists !", Toast.LENGTH_LONG).show();
                                    resp = true;
                                    session.setFirstTime(false);
                                    Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                                    finish();


                                } else {
                                    Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                }
                            } catch (JSONException e) {
                                // JSON data was not returned, because an error at php script/mysql

                                e.printStackTrace(); //logging error

                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            Log.e(TAG, "Volley Error: " + error.getMessage()); //error in android part logged

                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            // Posting parameters to login url

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Secrt", "1");               //   json POST paran add
                            return params;

                        }
                    };
                    // Adding request to request queue
                strReq.setRetryPolicy(new DefaultRetryPolicy(
                        1000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                {
                    //Toast.makeText(getBaseContext(), "You are on Mobile Data! \nSwitch Off Data and Restart App", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ConnectionActivity.this, ErrorActivityMob.class);
                    startActivity(intent);
                    finish();
                }
            }

        }
        else
        {
            Toasty.warning(getBaseContext(), "Turn on WiFi!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ConnectionActivity.this, ErrorActivityNowifi.class);
            startActivity(intent);
            finish();
        }
    }

    }


