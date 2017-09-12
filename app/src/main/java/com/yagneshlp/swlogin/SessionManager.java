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
    SharedPreferences pref1;

    Editor editor1;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME_LOGIN = "YLP_FirstTime";


    private static final String KEY_IS_LOGGEDIN = "FirstTime";


    public SessionManager(Context context) {
        this._context = context;
        pref1 = _context.getSharedPreferences(PREF_NAME_LOGIN, PRIVATE_MODE);
        editor1 = pref1.edit();

    }

    public void setFirstTime(boolean isLoggedIn) {
        editor1.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        // commit changes
        editor1.commit();
        Log.d(TAG, "User First time status");
    }

    public boolean isFirstTime(){

        return pref1.getBoolean(KEY_IS_LOGGEDIN, true);
    }


}