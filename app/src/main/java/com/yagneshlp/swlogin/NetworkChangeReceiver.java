package com.yagneshlp.swlogin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.attr.action;
import static android.graphics.Color.BLUE;
import static android.support.v4.app.NotificationCompat.CATEGORY_ALARM;
import static android.support.v4.app.NotificationCompat.CATEGORY_REMINDER;

/**
 * Created by Yagnesh L P on 03-10-2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkChangeReceiver.class.getSimpleName(); //for Logger Purposes
    boolean iswalledGarden;
    private static final String mWalledGardenUrl = "http://172.217.2.238/generate_204";
    private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;
    Context serviContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo ();
                String ssid  = info.getSSID();
                if(ssid.equals("\"HP\"") || ssid.equals("\"NITT-WiFi\"")) {
                    WifiManager wman = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo winfo = wman.getConnectionInfo();
                    if (winfo != null) {
                        serviContext=context;
                        new LongOperation().execute();
                    }
                }
                Log.d("Network Status ", "Connected to Wifi");
            }
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d("Network Status ", "Connecetd to Mobile data");

        } else {
            context.stopService(new Intent(context, PersistService.class));
            Log.d("Network Status ", "Disconnected from all networks");
        }
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
            } catch (IOException e) {

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
                Log.d(TAG, "Captive Portal found !!!");
                Intent notificationIntent = new Intent(serviContext, LaunchActivity.class);
                PendingIntent ci = PendingIntent.getActivity(serviContext, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(serviContext)
                                .setContentTitle("SWLogin")
                                .setContentText("Tap to Sign in")
                                .setPriority(2)
                                .setColor(serviContext.getResources().getColor(R.color.colorPrimaryDark))
                                .setSmallIcon(R.drawable.ic_notificon)
                                .setAutoCancel(true)
                                .setContentIntent(ci)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setCategory(CATEGORY_ALARM);

                Notification notification = mBuilder.build();

                NotificationManager notificationManager = (NotificationManager) serviContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1076, notification);
            }
            else
            {
                Log.d(TAG, "Captive Portal not found, So starting the service");
                serviContext.startService(new Intent(serviContext, PersistService.class));
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Checking if Captive Portal exists");
        }


    }
}


