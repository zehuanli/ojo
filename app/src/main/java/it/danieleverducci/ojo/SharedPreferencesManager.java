package it.danieleverducci.ojo;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String SP_FILE = "sp_file_name";//sharedPreference's name

    private static final String SP_ROTATION_ENABLED = "rot_en";

    public static void saveRotationEnabled(Context ctx, boolean enabled) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(SP_ROTATION_ENABLED, enabled).apply();
    }

    public static boolean loadRotationEnabled(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SP_ROTATION_ENABLED, false);
    }
}
