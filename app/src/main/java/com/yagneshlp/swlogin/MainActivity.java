package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;


/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class MainActivity extends Activity {

    SQLiteHandler db;
    SessionManager session;
    AlertDialog.Builder dialog;
    AlertDialog alertDialog;
    FloatingActionButton fab;
    HttpURLConnection conn;
    ProgressDialog progress ;
    int RemainingTime = 0;
    private static final String TAG = MainActivity.class.getSimpleName(); //for Logger Purposes

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        finish();
    }

    public void refresh(View v)
    {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void info(View v)
    {
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_out,R.anim.no_change);
        finish();
    }

    public void time(View v)
    {
        progress.setMessage("Fetching Time Left ... ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        disableSSLCertificateChecking();
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        new RemainingTimeRequest().execute();

    }

    public void yaglp(View v)
    {
        String url = "http://yagneshlp.com/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        String j="Yes";
        if(b!=null) {
            j = (String) b.get("ShouldShow");
        }

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if(j.equals("Yes")) {
            progress = new ProgressDialog(this);
            progress.setMessage("Fetching Time Left ... ");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
            disableSSLCertificateChecking();
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            new RemainingTimeRequest().execute();
        }
    }


    public void logout(View v)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Log Out?")
                .setMessage("You will be Connected to internet with the same Roll no till you login with new Roll no\n\nDo you Really want to Log out and log in with another Roll no? ");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                db.deleteUsers();
                                session.setFirstTime(true);
                                Intent intent = new Intent(MainActivity.this, CredActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                                finish();
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class RemainingTimeRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://192.168.20.1/usrHeartbeat.cgi"); // Defining the URL path

                conn = (HttpURLConnection) url.openConnection();   //Opening connection
                conn.setRequestProperty("Cookie", "domain=192.168.20.1; SessId=" + session.getCookieSess());  //Setting Cookies
                conn.setRequestProperty("Cookie", "domain=192.168.20.1; PageSeed=" + session.getCookiePage());  //SettingCookies
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();

                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);

                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("Error : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            Log.d(TAG, "Remaining Time Service Execute Result: " + result);
            String time = result.substring(114, 117);
            time = time.split(";")[0];
            RemainingTime = Integer.parseInt(time);
            progress.dismiss();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Time Left")
                    .setMessage("Your Current Session will expire after "+time+" minutes.");
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });



            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
        }};

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