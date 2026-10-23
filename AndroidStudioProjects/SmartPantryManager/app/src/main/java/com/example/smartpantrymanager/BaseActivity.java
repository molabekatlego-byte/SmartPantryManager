package com.example.smartpantrymanager;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared behaviour for every screen:
 * 1. keeps content clear of the status bar / navigation bar,
 * 2. wires up the bottom navigation bar (Pantry / Recipes / Settings).
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    /** Call from onCreate, after setContentView, on the three main screens. */
    protected void setupBottomNav(int selectedItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);          // set the highlighted tab first...
        nav.setOnItemSelectedListener(item -> {         // ...then listen for taps
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true;                            // already on this screen
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_suggested) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            startActivity(new Intent(this, target));    // Intent = "open this screen"
            finish();                                   // the three tabs replace each other
            return true;
        });
    }
}
