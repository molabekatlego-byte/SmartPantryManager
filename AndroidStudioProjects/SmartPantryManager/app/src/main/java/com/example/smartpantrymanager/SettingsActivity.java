package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;

/** Settings screen: toggle for highlighting items that are about to expire. */
public class SettingsActivity extends BaseActivity {

    public static final String PREFS = "settings";
    public static final String KEY_EXPIRING = "expiring_alert";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBottomNav(R.id.nav_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat sw = findViewById(R.id.switchExpiring);
        sw.setChecked(prefs.getBoolean(KEY_EXPIRING, true));
        sw.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRING, isChecked).apply());
    }
}
