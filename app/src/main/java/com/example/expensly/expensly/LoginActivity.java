package com.example.expensly.expensly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensly.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextView tvLogo;
    private ScrollView loginForm;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private final String logoText = "Expensly";
    private int characterIndex = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager session;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Überprüfung der Sitzung: Falls der Benutzer bereits bei Firebase angemeldet ist, Dashboard öffnen.
        session = new SessionManager(this);
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        // Aktiviert das Edge-to-Edge Design (Layout zieht sich hinter Status-/Navigationsleiste)
        WindowUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_login);
        WindowUtils.applyPadding(findViewById(R.id.login_root));

        tvLogo = findViewById(R.id.tv_logo_expensly);
        loginForm = findViewById(R.id.login_form_scroll);
        tilEmail = findViewById(R.id.til_email_login);
        tilPassword = findViewById(R.id.til_password_login);
        Button btnLogin = findViewById(R.id.btn_login);
        EditText etEmail = findViewById(R.id.email_login);
        EditText etPassword = findViewById(R.id.password_login);
        TextView tvSignup = findViewById(R.id.signup);
        TextView tvForgotPassword = findViewById(R.id.forgot_password);

        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
             // For now, just a toast
             Toast.makeText(this, "Reset link sent (simulation)", Toast.LENGTH_SHORT).show();
        });

        // Position logo in center initially
        tvLogo.post(() -> {
            float screenHeight = getResources().getDisplayMetrics().heightPixels;
            float logoCenterY = tvLogo.getY() + (tvLogo.getHeight() / 2.0f);
            float targetCenterY = screenHeight / 2.0f;
            tvLogo.setTranslationY(targetCenterY - logoCenterY);
        });

        startTypingAnimation();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (!validateInput(email, password)) return;

            // Anmeldung via Firebase Authentication
            btnLogin.setEnabled(false);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            onLoginSuccess(email);
                        } else {
                            btnLogin.setEnabled(true);
                            tilPassword.setError("Anmeldung fehlgeschlagen: E-Mail oder Passwort falsch");
                        }
                    });
        });
    }

    private void onLoginSuccess(String email) {
        String uid = auth.getCurrentUser().getUid();
        UserRepository.getInstance().getProfile(uid)
                .addOnCompleteListener(this, task -> {
                    String firstName = task.isSuccessful()
                            ? UserRepository.firstName(task.getResult()) : null;
                    session.login(email.toLowerCase(Locale.ROOT), firstName != null ? firstName : "User");
                    goToMain();
                });
    }

    private boolean validateInput(String email, String password) {
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valid = true;
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        }
        return valid;
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startTypingAnimation() {
        characterIndex = 0;
        tvLogo.setText("");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (characterIndex <= logoText.length()) {
                    tvLogo.setText(logoText.substring(0, characterIndex));
                    characterIndex++;
                    handler.postDelayed(this, 90);
                } else {
                    handler.postDelayed(() -> moveLogoToHeader(), 300);
                }
            }
        }, 300);
    }

    private void moveLogoToHeader() {
        tvLogo.animate()
                .translationY(0)
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    loginForm.setVisibility(View.VISIBLE);
                    loginForm.animate()
                            .alpha(1f)
                            .setDuration(400)
                            .start();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
