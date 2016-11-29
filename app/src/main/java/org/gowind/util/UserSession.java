package org.gowind.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.gowind.model.User;

/**
 * Created by shiv.loka on 11/2/16.
 */

public class UserSession {

    private SharedPreferences sharedPreferences;
    private static final String DEFAULT_USERNAME = "Default User";

    public UserSession(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setUser(String username) {
        sharedPreferences.edit().putString("username", username).commit();
    }

    public String getUser() {
        String username = sharedPreferences.getString("username", DEFAULT_USERNAME);
        return username;
    }

    public void setDistance(String totalDistance) {
        sharedPreferences.edit().putString("distance", totalDistance).commit();
    }

    public int getDistance() {
        return sharedPreferences.getInt("distance", 1);
    }

    public void setDuration() {
        sharedPreferences.edit().putInt("duration", 1).commit();
    }

    public int getDuration() {
        return sharedPreferences.getInt("duration", 1);
    }
}
