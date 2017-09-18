package com.yagneshlp.swlogin;

/**
 * Created by Yagnesh L P on 05-09-2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref1,pref2,pref3;

    Editor editor1,editor2,editor3;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME_LOGIN = "YLP_FirstTime";
    private static final String PREF_NAME_cookie1 = "YLP_Cookie1";
    private static final String PREF_NAME_cookie2 = "YLP_Cookie2";


    private static final String KEY_IS_LOGGEDIN = "FirstTime";
    private static final String KEY_Cookie1 = "Cookie1";
    private static final String KEY_Cookie2 = "Cookie2";


    public SessionManager(Context context) {
        this._context = context;
        pref1 = _context.getSharedPreferences(PREF_NAME_LOGIN, PRIVATE_MODE);
        pref2 = _context.getSharedPreferences(PREF_NAME_cookie1, PRIVATE_MODE);
        pref3 = _context.getSharedPreferences(PREF_NAME_cookie2, PRIVATE_MODE);
        editor1 = pref1.edit();
        editor2 = pref2.edit();
        editor3 = pref3.edit();
    }

    public void setFirstTime(boolean isLoggedIn) {
        editor1.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        // commit changes
        editor1.commit();
        Log.d(TAG, "User First time status");
    }
    public void setCookieSess(String cookie1) {
        editor2.putString(KEY_Cookie1, cookie1);
        // commit changes
        editor2.commit();
        Log.d(TAG, "New Cookie for SessID set");
    }
    public void setCookePage(String cookie2) {
        editor3.putString(KEY_Cookie2, cookie2);
        // commit changes
        editor3.commit();
        Log.d(TAG, "New cookie for pageSeed set");
    }

    public boolean isFirstTime(){

        return pref1.getBoolean(KEY_IS_LOGGEDIN, true);
    }

    public String getCookieSess(){

        return pref2.getString(KEY_Cookie1,"null");
    }

    public String getCookiePage(){

        return pref3.getString(KEY_Cookie2, "null");
    }


}