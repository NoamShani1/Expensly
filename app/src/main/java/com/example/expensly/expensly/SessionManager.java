package com.example.expensly.expensly;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Speichert den Status der aktuellen Benutzeranmeldung dauerhaft in den SharedPreferences.
 * So bleibt der Benutzer auch nach dem Schließen der App eingeloggt.
 */
public class SessionManager {

    private static final String PREFS_NAME = "expensly_session";
    private static final String KEY_EMAIL  = "email";
    private static final String KEY_FNAME  = "first_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Speichert die E-Mail und den Vornamen des angemeldeten Benutzers. */
    public void login(String email, String firstName) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_FNAME, firstName != null ? firstName : "")
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_EMAIL);
    }

    /** Email of the logged-in user, or null if nobody is logged in. */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFirstName() {
        return prefs.getString(KEY_FNAME, "");
    }

    /** Keeps the cached first name in sync after the user edits their profile. */
    public void setFirstName(String firstName) {
        prefs.edit().putString(KEY_FNAME, firstName != null ? firstName : "").apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
