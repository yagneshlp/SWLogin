package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.UUID;
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

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        Log.d(TAG, "New Session Initiated. Generating new cookies");
        session.setCookieSess(generateString(1));
        session.setCookePage(generateString(2));
        Log.d(TAG, "Generated Cookies: "+"cookie1: " + session.getCookieSess() + " Cookie2: " + session.getCookiePage());

        wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);

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


    private class GetClass1 extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass1(Context c){
            this.context = c;
        }

        protected void onPreExecute(){ }

        @Override
        protected Void doInBackground(String... params) {
            try {


                URL url = new URL("https://192.168.20.1/auth1.html");

                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Cookie","domain=192.168.20.1; SessId="+session.getCookieSess());
                conn.setRequestProperty("Cookie","domain=192.168.20.1; PageSeed="+session.getCookiePage());
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                Log.e(TAG, "GET 1  Result: " + output);


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }}
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
                conn.setRequestProperty("Cookie","domain=192.168.20.1; SessId="+session.getCookieSess());
                conn.setRequestProperty("Cookie","domain=192.168.20.1; PageSeed="+session.getCookiePage());
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
                    }
                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("Error : "+responseCode);
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
                Toasty.error(getApplicationContext(), "Please Check your Credentials and try again !",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, CredActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                finish();

            }
            else {

            }
            Log.e(TAG, "Login Result: " + result);
        }
    }

    private class GetClass2 extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass2(Context c){
            this.context = c;
        }

        protected void onPreExecute(){  }

        @Override
        protected Void doInBackground(String... params) {
            try {


                URL url = new URL("https://192.168.20.1/loginStatusTop(eng).html");

                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Cookie","domain=192.168.20.1; SessId="+session.getCookieSess());
                conn.setRequestProperty("Cookie","domain=192.168.20.1; PageSeed="+session.getCookiePage());
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();
                Log.e(TAG, "GET 2  Result: " + output);


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }}

    public class SendPost2Request extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://192.168.20.1/usrHeartbeat.cgi"); // here is your URL path


                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Cookie","domain=192.168.20.1; SessId="+session.getCookieSess());
                conn.setRequestProperty("Cookie","domain=192.168.20.1; PageSeed="+session.getCookiePage());
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

                Log.d(TAG, "Response end = " + result);
                String time = result.substring(114, 117);
                time = time.split(";")[0];
                //Toast.makeText(getApplicationContext(), "Time left = " + time, Toast.LENGTH_LONG).show();
                int RemainingTime = Integer.parseInt(time);
                if(RemainingTime != 180 )
                {
                    Toasty.error(getApplicationContext(), "Login Not Successful \nCheck your Credentials", Toast.LENGTH_SHORT, true).show();
                    Intent intent = new Intent(LoginActivity.this, CredActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                    finish();
                }
                else {
                    Toasty.success(getApplicationContext(), "Successfully Connected !", Toast.LENGTH_SHORT, true).show();
                    session.setFirstTime(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_out, R.anim.no_change);
                    finish();
                }

        }
    }

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

    public static String generateString(int useCase) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        if(useCase == 1)
        {
            uuid = uuid.toUpperCase();
            return uuid;
        }
        else if( useCase ==2)
        {
            return uuid;
        }
        return "null";

    }


}
