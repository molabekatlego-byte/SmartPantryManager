package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Pantry List screen (the first screen of the app). */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNav(R.id.nav_pantry);

        db = DatabaseHelper.getInstance(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume runs every time we come back to this screen, so the list is always fresh.
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
        adapter.setHighlightExpiring(prefs.getBoolean(SettingsActivity.KEY_EXPIRING, true));
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent i = new Intent(this, AddEditIngredientActivity.class);
        i.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.id); // pass data with the Intent
        startActivity(i);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.name + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.id);
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
