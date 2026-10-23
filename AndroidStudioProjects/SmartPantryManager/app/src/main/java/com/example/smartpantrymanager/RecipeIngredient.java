package com.example.smartpantrymanager;

/** One ingredient line of a recipe, e.g. 200 g flour. */
public class RecipeIngredient {
    public int recipeId;
    public String name;
    public double quantity;
    public String unit;

    public RecipeIngredient(int recipeId, String name, double quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
}
