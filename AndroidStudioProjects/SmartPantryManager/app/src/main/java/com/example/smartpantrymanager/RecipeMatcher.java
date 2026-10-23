package com.example.smartpantrymanager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The core business logic: STRICT recipe matching.
 * A recipe can only be suggested if EVERY ingredient is in the pantry
 * in at least the required quantity.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {}

    /** Makes "Tomatoes " and "tomato" look the same (simple plural handling). */
    public static String normalizeName(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("\\s+", " ");
        if (s.endsWith("ies") && s.length() > 4) {
            return s.substring(0, s.length() - 3) + "y";   // berries -> berry
        }
        if (s.endsWith("oes")) {
            return s.substring(0, s.length() - 2);          // tomatoes -> tomato
        }
        if (s.endsWith("ss")) {
            return s;                                       // watercress stays
        }
        if (s.endsWith("s") && s.length() > 3) {
            return s.substring(0, s.length() - 1);          // eggs -> egg
        }
        return s;
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }

    /** Units can only be compared inside the same family. */
    private static String unitFamily(String unit) {
        switch (clean(unit)) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            default:
                return "count"; // pcs
        }
    }

    /** How many base units (g, ml or pieces) one of this unit equals. */
    private static double unitFactor(String unit) {
        switch (clean(unit)) {
            case "kg":
            case "l":
                return 1000;
            case "tsp":
                return 5;
            case "tbsp":
                return 15;
            case "cup":
                return 240;
            default:
                return 1;
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + unitFamily(unit);
    }

    /** Adds up everything in the pantry, in base units, per ingredient. */
    public static Map<String, Double> buildTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem p : pantry) {
            String k = key(p.name, p.unit);
            double base = p.quantity * unitFactor(p.unit);
            Double existing = totals.get(k);
            totals.put(k, (existing == null ? 0.0 : existing) + base);
        }
        return totals;
    }

    /** STRICT rule: one missing or short ingredient rejects the whole recipe. */
    public static boolean canMake(List<RecipeIngredient> needed, Map<String, Double> totals) {
        if (needed.isEmpty()) {
            return false;
        }
        for (RecipeIngredient ing : needed) {
            double need = ing.quantity * unitFactor(ing.unit);
            Double haveObj = totals.get(key(ing.name, ing.unit));
            double have = haveObj == null ? 0.0 : haveObj;
            if (have + 0.0001 < need) {
                return false;
            }
        }
        return true;
    }
}
