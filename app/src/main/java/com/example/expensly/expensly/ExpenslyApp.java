package com.example.expensly.expensly;

import android.app.Application;

import com.example.expensly.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Debug builds talk to the local Firebase Emulator Suite instead of production Firebase,
 * so sign-up/sign-in work offline without Play Integrity/reCAPTCHA device attestation.
 * 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
 */
public class ExpenslyApp extends Application {

    private static final String EMULATOR_HOST = "10.0.2.2";

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FirebaseAuth.getInstance().useEmulator(EMULATOR_HOST, 9099);
            FirebaseFirestore.getInstance().useEmulator(EMULATOR_HOST, 8080);
        }
    }
}
