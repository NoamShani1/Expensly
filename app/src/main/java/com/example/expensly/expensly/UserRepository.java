package com.example.expensly.expensly;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores/retrieves user profile data (first name, last name, email) in Firestore.
 * Auth itself (credentials, sessions) is handled by FirebaseAuth.
 */
public class UserRepository {

    private static final String COLLECTION_USERS = "users";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_LAST_NAME  = "lastName";
    private static final String FIELD_EMAIL      = "email";

    private static UserRepository instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private UserRepository() {
    }

    public Task<Void> createProfile(@NonNull String uid, String firstName, String lastName, String email) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FIRST_NAME, firstName);
        data.put(FIELD_LAST_NAME, lastName);
        data.put(FIELD_EMAIL, email);
        return db.collection(COLLECTION_USERS).document(uid).set(data);
    }

    public Task<DocumentSnapshot> getProfile(@NonNull String uid) {
        return db.collection(COLLECTION_USERS).document(uid).get();
    }

    public Task<Void> updateName(@NonNull String uid, String firstName, String lastName) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FIRST_NAME, firstName);
        data.put(FIELD_LAST_NAME, lastName);
        return db.collection(COLLECTION_USERS).document(uid).update(data);
    }

    public static String firstName(DocumentSnapshot doc) {
        return doc != null ? doc.getString(FIELD_FIRST_NAME) : null;
    }

    public static String lastName(DocumentSnapshot doc) {
        return doc != null ? doc.getString(FIELD_LAST_NAME) : null;
    }
}
