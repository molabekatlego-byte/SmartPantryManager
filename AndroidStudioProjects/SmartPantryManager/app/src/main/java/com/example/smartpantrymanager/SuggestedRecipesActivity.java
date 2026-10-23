package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Suggested Recipes screen: only recipes the user can make right now. */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recycler;
    private TextView tvNoMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setupBottomNav(R.id.nav_suggested);

        db = DatabaseHelper.getInstance(this);
        tvNoMatches = findViewById(R.id.tvNoMatches);
        recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMatches(); // the pantry may have changed, so recalculate every time
    }

    private void loadMatches() {
        List<PantryItem> pantry = db.getAllPantryItems();
        Map<String, Double> totals = RecipeMatcher.buildTotals(pantry);

        List<Recipe> matches = new ArrayList<>();
        for (Recipe r : db.getAllRecipes()) {
            List<RecipeIngredient> needed = db.getIngredients(r.id);
            if (RecipeMatcher.canMake(needed, totals)) {   // the strict-matching rule
                matches.add(r);
            }
        }

        adapter.setRecipes(matches);
        boolean none = matches.isEmpty();
        tvNoMatches.setVisibility(none ? View.VISIBLE : View.GONE);
        recycler.setVisibility(none ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(i);
    }
}
