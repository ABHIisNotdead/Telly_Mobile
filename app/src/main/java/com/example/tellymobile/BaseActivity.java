package com.example.tellymobile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate()
        int themeId = ThemeManager.getThemeId(this);
        setTheme(ThemeManager.getThemeStyle(themeId));
        
        super.onCreate(savedInstanceState);
    }
}
