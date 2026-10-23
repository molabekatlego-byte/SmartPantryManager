package com.example.smartpantrymanager;

/** Small formatting helper so 3.0 is shown as "3". */
public final class Fmt {
    private Fmt() {}

    public static String qty(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }
}
