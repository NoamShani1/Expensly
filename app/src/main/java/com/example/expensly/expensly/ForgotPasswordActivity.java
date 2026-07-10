package com.example.expensly.expensly;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensly.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextInputLayout tilEmail;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_forgot_password);
        WindowUtils.applyPadding(findViewById(R.id.forgot_root));

        etEmail = findViewById(R.id.email_reset);
        tilEmail = findViewById(R.id.til_email_reset);

        // Firebase resets passwords via an emailed link, not by typing a new one here.
        findViewById(R.id.til_new_password).setVisibility(View.GONE);
        findViewById(R.id.til_confirm_new_password).setVisibility(View.GONE);

        btnReset = findViewById(R.id.btn_reset_password);
        btnReset.setText("Send Reset Link");
        btnReset.setOnClickListener(v -> handleReset());

        findViewById(R.id.back_to_login).setOnClickListener(v -> finish());
    }

    private void handleReset() {
        String email = etEmail.getText().toString().trim();
        tilEmail.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }

        btnReset.setEnabled(false);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    btnReset.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Couldn't send reset email. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
