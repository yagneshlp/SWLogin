package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import es.dmoral.toasty.Toasty;


/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class ConnectionActivity extends Activity {

    boolean resp = false;
    SessionManager session;
    private static final String TAG = ConnectionActivity.class.getSimpleName(); //for Logger Purposes
    ProgressDialog progress ;
    HttpURLConnection conn;
    boolean iswalledGarden;
    private static final String mWalledGardenUrl = "http://172.217.2.238/generate_204";
    private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;

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
                new LongOperation().execute();
                    /*
                    String tag_string_req = "Ping Request";
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
                            Log.e(TAG, "Volley Error: " + error.getMessage() + "(Expected and Handled)"); //error in android part logged

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
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);*/


            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                {
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

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mWalledGardenUrl); // "http://clients3.google.com/generate_204"
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
                urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
                urlConnection.setUseCaches(false);
                urlConnection.getInputStream();
                // We got a valid response, but not from the real google
                iswalledGarden = urlConnection.getResponseCode() != 204;
            } catch( SocketTimeoutException e){
                //TODO timeout,proper connection not existant. show a dialog asking to wheater try again
            }
            catch (IOException e) {

                Log.e(TAG,"Walled garden check - probably not a portal: exception "+ e);
                iswalledGarden = false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }


           return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(iswalledGarden)
            {
                Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                resp = true;
                session.setFirstTime(false);
                Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Checking if internet connection exists");
        }


    }



    private boolean isWalledGardenConnection() {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mWalledGardenUrl); // "http://clients3.google.com/generate_204"
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {

            Log.e(TAG,"Walled garden check - probably not a portal: exception "+ e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}


