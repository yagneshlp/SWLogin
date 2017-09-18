package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class LaunchActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session;
        session = new SessionManager(getApplicationContext());
        if(session.isFirstTime())
        {
            Intent intent = new Intent(LaunchActivity.this, CredActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Intent intent = new Intent(LaunchActivity.this, ConnectionActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
