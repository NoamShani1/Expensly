package com.example.expensly.expensly;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Edge-to-edge handling shared by every screen.
 *
 * From targetSdk 35+ the system draws behind the status and navigation bars,
 * so each Activity must pad its root view by the system bar insets — otherwise
 * content (and touch targets like the logout button) end up underneath them.
 */
public final class WindowUtils {

    private WindowUtils() {
    }

    /**
     * Pads the given root view by the system bars, display cutout and keyboard
     * insets, and makes the status/navigation bar icons light so they are
     * visible on the dark gradient background.
     */
    public static void applyEdgeToEdge(Activity activity, View root) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
