package com.example.expensly.expensly;

import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Modern Edge-to-Edge handling.
 * Uses the official androidx.activity.EdgeToEdge API.
 */
public final class WindowUtils {

    private WindowUtils() {}

    /**
     * Call this BEFORE setContentView in Activity.onCreate.
     */
    public static void enableEdgeToEdge(ComponentActivity activity) {
        EdgeToEdge.enable(activity);
    }

    /**
     * Call this AFTER setContentView to apply padding to the root view.
     */
    public static void applyPadding(View root) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() |
                    WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
    
    /**
     * Legacy helper that does both (for convenience).
     * Make sure to call this before setContentView if you use the returned layout params,
     * but usually it's easier to just call the two methods above.
     */
    public static void applyEdgeToEdge(ComponentActivity activity, View root) {
        enableEdgeToEdge(activity);
        applyPadding(root);
    }
}
