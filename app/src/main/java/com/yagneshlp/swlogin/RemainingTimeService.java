package com.yagneshlp.swlogin;

/**
 * Created by Yagnesh L P on 14-09-2017.
 */

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.graphics.Color.BLUE;
import static android.support.v4.app.NotificationCompat.CATEGORY_REMINDER;


public class RemainingTimeService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    HttpURLConnection conn;
    String SessCook, PageCook;
    SessionManager session;
    int RemainingTime = 0;
    private static final String TAG = "Remaining Time Service ";//for Logger Purposes

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        session = new SessionManager(getApplicationContext());
        SessCook = session.getCookieSess();
        PageCook = session.getCookiePage();
        disableSSLCertificateChecking();
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        new RemainingTimeRequest().execute();
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    public class RemainingTimeRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://192.168.20.1/usrHeartbeat.cgi"); // Defining the URL path

                conn = (HttpURLConnection) url.openConnection();   //Opening connection
                conn.setRequestProperty("Cookie", "domain=192.168.20.1; SessId=" + SessCook);  //Setting Cookies
                conn.setRequestProperty("Cookie", "domain=192.168.20.1; PageSeed=" + PageCook);  //SettingCookies
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
            //Toast.makeText(getApplicationContext(), "Time left = " + time, Toast.LENGTH_LONG).show();
            RemainingTime = Integer.parseInt(time);
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("Remaining Time ", RemainingTime);
            startActivity(intent);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
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
            //Toast.makeText(getApplicationContext(), "Time left = "+time,Toast.LENGTH_LONG).show();
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(RemainingTimeService.this)
                            .setContentTitle("Time Left")
                            .setContentText("Your Session will get over after " + RemainingTime + " minutes")
                            .setPriority(2)
                            .setSmallIcon(R.drawable.ic_info)
                            .setAutoCancel(true)
                            //.setOngoing(true)
                            .setCategory(CATEGORY_REMINDER);


            //Notification notification = mBuilder.build();

            //NotificationManager notificationManager = (NotificationManager) RemainingTimeService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            //notificationManager.notify(m,notification);
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