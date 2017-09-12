package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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
        String url = "http://192.168.20.1/loginStatusTop(eng).html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        fab = (FloatingActionButton) findViewById(R.id.fab);



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

}