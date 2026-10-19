package com.mowtiie.easybank;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("easybank_preferences", MODE_PRIVATE);
    }

    public void setUserId(int userId) {
        sharedPreferences.edit().putInt("user_id", userId).apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt("user_id", -1);
    }
}
