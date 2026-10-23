package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

/** Recipe Detail screen: full ingredient list and method. */
public class RecipeDetailActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        int id = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = db.getRecipe(id);
        if (recipe == null) {
            finish(); // safety: unknown recipe id
            return;
        }

        ((TextView) findViewById(R.id.tvDetailName)).setText(recipe.title);

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : db.getIngredients(id)) {
            sb.append("\u2022 ").append(Fmt.qty(ing.quantity)).append(" ")
                    .append(ing.unit).append(" ").append(ing.name).append("\n");
        }
        ((TextView) findViewById(R.id.tvIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tvSteps)).setText(recipe.instructions);
    }
}
