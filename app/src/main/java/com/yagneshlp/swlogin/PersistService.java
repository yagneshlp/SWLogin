package com.yagneshlp.swlogin;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;

import static android.support.v4.app.NotificationCompat.CATEGORY_ERROR;
import static android.support.v4.app.NotificationCompat.CATEGORY_MESSAGE;
import static android.support.v4.app.NotificationCompat.CATEGORY_REMINDER;

/**
 * Created by Yagnesh L P on 20-09-2017.
 */

public class PersistService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    HttpURLConnection conn;
    String SessCook, PageCook;
    SessionManager session;
    int RemainingTime = 0;
    int notifID;
    String roll,pass;
    boolean hibernate=false;
    private SQLiteHandler db;
    private static final String TAG = "Persist Service ";//for Logger Purposes

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Persist Service Starts ");
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "Network identified as Wifi");
                session = new SessionManager(getApplicationContext());
                db = new SQLiteHandler(getApplicationContext());
                SessCook = session.getCookieSess();
                PageCook = session.getCookiePage();
                roll = db.getRoll();
                pass = db.getPass();
                Random random = new Random();
                notifID = 10;
                disableSSLCertificateChecking();
                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                new PersistService.RemainingTimeRequest().execute();
            }
            else if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                Log.d(TAG, "Network identified as Mobile data, shutting down");
                stopSelf();
            }
        } else{
            Log.d(TAG, "No network was found, shutting down");
            stopSelf();
        }


        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //dismissNotif();
        Log.d(TAG, "Persist Service Ends");

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
                    Log.d(TAG, "Persist Service Execute Result: Error: " + responseCode);
                    return new String("Error occured and logged");
                }
            } catch (Exception e) {

                Log.d(TAG, "Persist Service Execute Result: Exception: " + e.getMessage());
                if(e.getMessage().equals("Network is unreachable"))
                    hibernate=true;
                return new String("Exception occured and logged");
            }

        }

        @Override
        protected void onPostExecute(String result) {
            String time=null;
            Log.d(TAG, "Persist Service Execute Result: " + result);
            try{
                 time = result.substring(114, 117);

            }
            catch (StringIndexOutOfBoundsException e)
            {
                Log.d(TAG, "StringIndexOutOfBoundsException occured and handled. Might also have occured due to error/exception");
                RemainingTime=-1;
                isItTimeToLogin();
                return;
            }
            time = time.split(";")[0];

            //Toast.makeText(getApplicationContext(), "Time left = " + time, Toast.LENGTH_LONG).show();
            RemainingTime = Integer.parseInt(time);
            isItTimeToLogin();
            //Intent intent = new Intent(getBaseContext(), MainActivity.class);
            //intent.putExtra("Remaining Time ", RemainingTime);
            //startActivity(intent);
            /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
            alertDialogBuilder.setTitle("Time Left")
                    .setMessage("Your Current Session will expire after "+time+" minutes.");
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });



            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/
            //Toast.makeText(getApplicationContext(), "Time left = "+time,Toast.LENGTH_LONG).show();



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

    private void isItTimeToLogin()
    {
        if(RemainingTime <= 20 && RemainingTime >0)
        {
            /*Log.d(TAG, "New Session Initiated. Generating new cookies");
            session.setCookieSess(generateString(1));
            session.setCookePage(generateString(2));
            Log.d(TAG, "Generated Cookies: "+"Cookie1: " + session.getCookieSess() + " Cookie2: " + session.getCookiePage());
            disableSSLCertificateChecking();
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            new GetClass1(this).execute();
            new SendPost1Request().execute();
            new GetClass2(this).execute();
            new SendPost2Request().execute();*/
            //add a feature to change the notification content to sign in soon
            updateNotif(3);
        }
        else
        {
            if(RemainingTime == -1)
                updateNotif(1);
            else
                updateNotif(2);

            Intent intent = new Intent(getBaseContext(), PersistService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    getBaseContext(), 1, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, 10000,
                    pendingIntent);
            stopSelf();
            /*
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new PersistService.RemainingTimeRequest().execute();
                }
            }, 30000);*/

        }

    }

    private void updateNotif(int state) {
        if(state ==2) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent ci = PendingIntent.getActivity(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(PersistService.this)
                            .setContentTitle("Status")
                            .setContentText("Connected to Internet :D")
                            .setPriority(0)
                            .setContentIntent(ci)
                            .setColor(getResources().getColor(R.color.colorPrimaryDark))
                            .setSmallIcon(R.drawable.ic_notificon)
                            .setOngoing(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setCategory(CATEGORY_MESSAGE);
            Notification notification = mBuilder.build();
            NotificationManager notificationManager = (NotificationManager) PersistService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifID, notification);
        }
        else if(state ==1 )
        {
            String Message;
            if(hibernate)
                Message="Hibernated";
            else
                Message = "Currently Experiencing Some Issue, Hold on!\nRestoring Connection ";

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(PersistService.this)
                            .setContentTitle("Status")
                            .setContentText(Message)
                            .setPriority(1)
                            .setColor(getResources().getColor(R.color.colorPrimaryDark))
                            .setSmallIcon(R.drawable.ic_notificon)
                            .setOngoing(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setCategory(CATEGORY_ERROR);
            Notification notification = mBuilder.build();
            NotificationManager notificationManager = (NotificationManager) PersistService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifID, notification);
        }
        else
        {
            Intent notificationIntent = new Intent(this, LaunchActivity.class);
            PendingIntent ci = PendingIntent.getActivity(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(PersistService.this)
                            .setContentTitle("Status")
                            .setContentText("Session is about to expire soon.\nTap to login")
                            .setPriority(2)
                            .setContentIntent(ci)
                            .setColor(getResources().getColor(R.color.colorPrimaryDark))
                            .setSmallIcon(R.drawable.ic_notificon)
                            .setOngoing(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setCategory(CATEGORY_REMINDER);
            Notification notification = mBuilder.build();
            NotificationManager notificationManager = (NotificationManager) PersistService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifID, notification);
        }

    }
    public void dismissNotif()
    {
        NotificationManager notificationManager = (NotificationManager) PersistService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifID);

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
                Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                //TODO error handler
                e.printStackTrace();
            } catch (IOException e) {
                Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                //TODO error handler
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

                    Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                    //TODO error handler
                    return new String("Error : "+responseCode);
                }
            }
            catch(Exception e){
                Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                //TODO error handler
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            boolean found = result.contains("refresh=true");
            if(found)
            {
                Toasty.error(getApplicationContext(), "Login Failed :(\nPlease Check your Credentials and try again",Toast.LENGTH_LONG).show();
                //TODO error handler

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
               //TODO display error notif
                e.printStackTrace();
            } catch (IOException e) {
                //TODO display error notif
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
                    Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                    //TODO display error notif
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                Toasty.error(getApplicationContext(), "Login Failed.\nTry Again Later",Toast.LENGTH_LONG).show();
                //TODO display error notif
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            Log.d(TAG, "Response end = " + result);
            String time = result.substring(114, 117);
            time = time.split(";")[0];
            //Toast.makeText(getApplicationContext(), "Time left = " + time, Toast.LENGTH_LONG).show();
            RemainingTime = Integer.parseInt(time);
            if(RemainingTime != 180 )
            {
                Toasty.error(getApplicationContext(), "Login Not Successful \nCheck your Credentials", Toast.LENGTH_SHORT, true).show();
                //TODO error handler
            }
            else {
                Toasty.success(getApplicationContext(), "Successfully Connected !", Toast.LENGTH_SHORT, true).show();
                session.setFirstTime(false);
                isItTimeToLogin();
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
