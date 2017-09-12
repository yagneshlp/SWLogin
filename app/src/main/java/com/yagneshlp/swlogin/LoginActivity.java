package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;

/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class LoginActivity extends Activity {

    String roll,pass;
    private SQLiteHandler db;
    SessionManager session;
    private static final String TAG = LoginActivity.class.getSimpleName(); //for Logger Purposes
    WifiManager wifiManager;
    ProgressDialog progress ;
    int counter=0;
    HttpURLConnection conn;

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        progress.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        //wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        progress = new ProgressDialog(this);
        roll = db.getRoll();
        pass = db.getPass();
        progress.setMessage("Logging in ... ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
       process();

    }
    private void process()
    {
        if(counter==1) {
            progress.setMessage("Logging in ... \nPass one failed, trying once more ");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }
        disableSSLCertificateChecking();
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        new GetClass1(this).execute();
        new SendPost1Request().execute();
        new GetClass2(this).execute();
        new SendPost2Request().execute();
    }

    public class SendPost1Request extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://192.168.20.1/auth.cgi"); // here is your URL path

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uName", roll);
                postDataParams.put("pass", pass);
                Log.e("params",postDataParams.toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getApplicationContext(), result,
            // Toast.LENGTH_LONG).show();
            Log.e("LoginActivity", "execute result " + result);
        }
    }

    public class SendPost2Request extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://192.168.20.1/usrHeartbeat.cgi"); // here is your URL path


                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();

                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            boolean found = result.contains("refresh=true");
            if(found)
            {
                Toast.makeText(getApplicationContext(), "Try again Later !",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, CredActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                finish();

            }
            else {
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
                                Toasty.success(getApplicationContext(), "Successfully Connected !", Toast.LENGTH_SHORT, true).show();
                                session.setFirstTime(false);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                                finish();


                            } else {
                                if(counter==0) {
                                    wifiManager.setWifiEnabled(false);
                                    wifiManager.setWifiEnabled(true);
                                    counter++;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.dismiss();
                                            process();
                                        }
                                    }, 4000);

                                }
                                else
                                {   Toasty.error(getApplicationContext(), "Error Occured \nProblem might be with the credentials !", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(LoginActivity.this, CredActivity.class);
                                    //db.deleteUsers();
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        } catch (JSONException e) {
                            // JSON data was not returned, because an error at php script/mysql

                            e.printStackTrace(); //logging error

                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toasty.error(getApplicationContext(), "Error Occured \nProblem might be with the credentials !", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, CredActivity.class);
                        //db.deleteUsers();
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
                        2000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

            }

            Log.e("MainAct", "LoginResponse: " + result.toString());
        }
    }

    private class GetClass1 extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass1(Context c){
            this.context = c;
        }

        protected void onPreExecute(){

        }

        @Override
        protected Void doInBackground(String... params) {
            try {


                URL url = new URL("https://192.168.20.1/auth1.html");

                 conn = (HttpURLConnection)url.openConnection();
                String urlParameters = "fizz=buzz";
                conn.setRequestMethod("GET");
                conn.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                conn.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = conn.getResponseCode();

                ///System.out.println("\nSending 'POST' request to URL : " + url);
                //System.out.println("Post parameters : " + urlParameters);
                //System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("Request URL " + url);
                //output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                //System.out.println("output===============" + br);
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                LoginActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //outputView.setText(output);
                        //progress.dismiss();

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }}

    private class GetClass2 extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass2(Context c){
            this.context = c;
        }

        protected void onPreExecute(){

        }

        @Override
        protected Void doInBackground(String... params) {
            try {


                URL url = new URL("https://192.168.20.1/loginStatusTop(eng).html");

                conn = (HttpURLConnection)url.openConnection();

                conn.setRequestMethod("GET");
                //connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                //connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = conn.getResponseCode();

                ///System.out.println("\nSending 'POST' request to URL : " + url);
                //System.out.println("Post parameters : " + urlParameters);
                //System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("Request URL " + url);
                //output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                //System.out.println("output===============" + br);
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                LoginActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //outputView.setText(output);
                        //progress.dismiss();

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }}

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


}
