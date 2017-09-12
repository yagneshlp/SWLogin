package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

import tr.xip.errorview.ErrorView;

/**
 * Created by Yagnesh L P on 07-09-2017.
 */

public class ErrorActivityNowifi extends Activity {

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errorwifi);

        ErrorView mErrorView = findViewById(R.id.error_view);
        mErrorView.setTitle("Oops!")
                .setTitleColor(getResources().getColor(R.color.white))
                .setSubtitle("Dude, Phone's not connected to WiFi\nConnect to a WiFi AP and press the retry button")
                .setSubtitleColor(getResources().getColor(R.color.whiteAlpha1))
                .setRetryText(R.string.error_view_retry);


        mErrorView.setRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                Intent intent = new Intent(ErrorActivityNowifi.this, LaunchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_out,R.anim.no_change);
                finish();
            }
        });
    }
}