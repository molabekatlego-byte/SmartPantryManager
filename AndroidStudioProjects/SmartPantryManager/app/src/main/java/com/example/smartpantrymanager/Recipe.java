package com.example.smartpantrymanager;

/** A recipe. Its ingredients live in a separate table (see RecipeIngredient). */
public class Recipe {
    public int id;
    public String title;
    public String instructions;

    public Recipe(int id, String title, String instructions) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
    }
}
