package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database for the Smart Pantry Manager.
 * Tables: pantry, recipes, recipe_ingredients (one recipe has many ingredient rows).
 * The recipes are pre-loaded the first time the database is created.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 2;

    private static final String TABLE_PANTRY = "pantry";
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_INGREDIENTS = "recipe_ingredients";

    private static DatabaseHelper instance;

    /** One shared helper for the whole app. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "instructions TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id) ON DELETE CASCADE)");

        seedRecipes(db); // pre-load recipes on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // Pantry CRUD
    // ------------------------------------------------------------------

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.name);
        v.put("quantity", item.quantity);
        v.put("unit", item.unit);
        v.put("expiry", item.expiry);
        return v;
    }

    /** CREATE */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    /** UPDATE */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item),
                "id = ?", new String[]{String.valueOf(item.id)});
    }

    /** DELETE */
    public int deletePantryItem(int id) {
        return getWritableDatabase().delete(TABLE_PANTRY,
                "id = ?", new String[]{String.valueOf(id)});
    }

    private PantryItem readPantryItem(Cursor c) {
        int expiryCol = c.getColumnIndexOrThrow("expiry");
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(expiryCol) ? null : c.getString(expiryCol));
    }

    /** READ (all) */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, "name COLLATE NOCASE")) {
            while (c.moveToNext()) {
                list.add(readPantryItem(c));
            }
        }
        return list;
    }

    /** READ (one) */
    public PantryItem getPantryItem(int id) {
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readPantryItem(c) : null;
        }
    }

    // ------------------------------------------------------------------
    // Recipes (read only for the user)
    // ------------------------------------------------------------------

    private Recipe readRecipe(Cursor c) {
        return new Recipe(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("instructions")));
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_RECIPES, null, null, null,
                null, null, "title COLLATE NOCASE")) {
            while (c.moveToNext()) {
                list.add(readRecipe(c));
            }
        }
        return list;
    }

    public Recipe getRecipe(int id) {
        try (Cursor c = getReadableDatabase().query(TABLE_RECIPES, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readRecipe(c) : null;
        }
    }

    public List<RecipeIngredient> getIngredients(int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_INGREDIENTS, null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, "id")) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        recipeId,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Seed data: 18 recipes. Ingredient format is "name|quantity|unit".
    // ------------------------------------------------------------------

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "Whisk the eggs with salt. Melt the butter in a pan. Pour in the eggs and stir gently until just set.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Cheese Omelette",
                "Whisk eggs with salt. Cook in a buttered pan, add grated cheese, fold and serve.",
                "egg|2|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Pancakes",
                "Mix flour, milk, egg and sugar into a smooth batter. Fry spoonfuls in a hot pan until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp");
        addRecipe(db, "Tomato Pasta",
                "Boil the pasta. Fry onion and garlic in olive oil, add chopped tomatoes and simmer for 10 minutes. Toss with the pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|2|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "Cook the rice. Scramble the eggs in oil, add chopped onion, rice and soy sauce. Stir-fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Mashed Potatoes",
                "Boil peeled potatoes until soft. Drain, then mash with butter, warm milk and salt.",
                "potato|500|g", "butter|30|g", "milk|100|ml", "salt|1|tsp");
        addRecipe(db, "Cheese Toast",
                "Butter the bread, top with cheese and grill until bubbling and golden.",
                "bread|4|pcs", "cheese|100|g", "butter|20|g");
        addRecipe(db, "Banana Smoothie",
                "Peel the bananas and blend them with milk and honey until smooth.",
                "banana|2|pcs", "milk|250|ml", "honey|1|tbsp");
        addRecipe(db, "Chicken Stir-Fry",
                "Slice the chicken, carrots and onion. Stir-fry the chicken in oil until cooked, add the vegetables and soy sauce and cook for 5 minutes.",
                "chicken|300|g", "carrot|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Vegetable Soup",
                "Chop the carrots, potatoes and onion. Simmer in the stock for 25 minutes and season with salt.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "vegetable stock|500|ml", "salt|1|tsp");
        addRecipe(db, "Garlic Bread",
                "Mash chopped garlic into the butter, spread on the bread and bake at 200C for 10 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");
        addRecipe(db, "Egg Noodles",
                "Boil the noodles. Scramble eggs in oil, add the noodles and soy sauce and toss for 2 minutes.",
                "noodles|200|g", "egg|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Tuna Pasta",
                "Boil the pasta. Warm the garlic in olive oil, stir in the tuna, then toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "olive oil|1|tbsp", "garlic|1|pcs");
        addRecipe(db, "Creamy Porridge",
                "Simmer the oats in milk for 5 minutes, stirring. Sweeten with sugar.",
                "oats|100|g", "milk|400|ml", "sugar|1|tbsp");
        addRecipe(db, "French Toast",
                "Whisk egg, milk and sugar. Dip the bread slices and fry until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");
        addRecipe(db, "Cheesy Baked Potatoes",
                "Bake the potatoes at 200C for 1 hour. Cut open and add butter, cheese and salt.",
                "potato|4|pcs", "cheese|100|g", "butter|20|g", "salt|1|tsp");
        addRecipe(db, "Rice and Beans",
                "Cook the rice. Fry the onion in oil, add the beans and heat through. Serve over the rice.",
                "rice|200|g", "beans|400|g", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Tomato Soup",
                "Fry the onion in butter, add chopped tomatoes and stock, simmer for 20 minutes, then blend.",
                "tomato|6|pcs", "onion|1|pcs", "vegetable stock|500|ml", "butter|20|g");
    }

    private void addRecipe(SQLiteDatabase db, String title, String instructions, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("title", title);
        rv.put("instructions", instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(TABLE_INGREDIENTS, null, iv);
        }
    }
}
