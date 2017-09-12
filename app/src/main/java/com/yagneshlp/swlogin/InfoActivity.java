package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Yagnesh L P on 10-09-2017.
 */

public class InfoActivity extends Activity {

    @Override
    public void onBackPressed()    {

        finish();
        startActivity(new Intent(InfoActivity.this,MainActivity.class));
        overridePendingTransition(R.anim.fade_out,R.anim.no_change);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void eula(View view)
    {
        String url = "http://slambook.yagneshlp.com/eula/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    public void policy(View view)
    {
        String url = "http://swlogin.yagneshlp.com/privacy/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    public void thirdparty(View view)
    {
        String url = "http://slambook.yagneshlp.com/thirdparty/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
