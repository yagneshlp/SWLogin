package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

/**
 * Created by Yagnesh L P on 06-09-2017.
 */

public class SplashActivity extends Activity {

    CircularFillableLoaders circload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        circload = (CircularFillableLoaders) findViewById(R.id.circularFillableLoaders);
        circload.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                circload.setProgress(25);

            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                circload.setProgress(50);

            }
        }, 400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                circload.setProgress(75);

            }
        }, 600);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                circload.setProgress(100);

            }
        }, 800);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                circload.animate().alpha(0f).setDuration(500).start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(SplashActivity.this, LaunchActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                        finishAffinity();
                        finish();

                    }
                }, 550);

            }
        }, 900);

    }

}
