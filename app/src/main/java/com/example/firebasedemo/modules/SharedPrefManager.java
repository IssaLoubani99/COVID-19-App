package com.example.firebasedemo.modules;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    // const
    private static final String SHARED_PREF_NAME = "covidsharedpref";
    private static final String KET_ACCESS_TOKEN = "token";

    // attributes
    private static Context sContext;
    private static SharedPrefManager instance;

    public SharedPrefManager(Context context) {
        sContext = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public boolean storeToken(String token) {
        SharedPreferences sharedPreferences = sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KET_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken() {
        return sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).getString(KET_ACCESS_TOKEN, null);
    }

}
