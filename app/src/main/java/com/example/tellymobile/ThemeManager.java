package com.example.tellymobile;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {

    private static final String PREF_NAME = "ThemePrefs";
    private static final String KEY_THEME = "selected_theme";

    public static final int THEME_DEFAULT = 0;
    public static final int THEME_OCEAN = 1;
    public static final int THEME_SUNSET = 2;
    public static final int THEME_NATURE = 3;
    public static final int THEME_NIGHT = 4;

    public static void saveTheme(Context context, int themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, themeId).apply();
    }

    public static int getThemeId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_DEFAULT);
    }

    public static int getThemeStyle(int themeId) {
        switch (themeId) {
            case THEME_OCEAN:
                return R.style.Theme_TellyMobile_Ocean;
            case THEME_SUNSET:
                return R.style.Theme_TellyMobile_Sunset;
            case THEME_NATURE:
                return R.style.Theme_TellyMobile_Nature;
            case THEME_NIGHT:
                return R.style.Theme_TellyMobile_Night;
            default:
                return R.style.Theme_TellyMobile;
        }
    }
}
