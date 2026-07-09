package com.example.expensly.expensly;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensly.R;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPass, etConfirmPass;
    private TextInputLayout tilEmail, tilNewPass, tilConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_forgot_password);
        WindowUtils.applyPadding(findViewById(R.id.forgot_root));

        etEmail = findViewById(R.id.email_reset);
        etNewPass = findViewById(R.id.new_password);
        etConfirmPass = findViewById(R.id.confirm_new_password);

        tilEmail = findViewById(R.id.til_email_reset);
        tilNewPass = findViewById(R.id.til_new_password);
        tilConfirmPass = findViewById(R.id.til_confirm_new_password);

        Button btnReset = findViewById(R.id.btn_reset_password);
        btnReset.setOnClickListener(v -> handleReset());

        findViewById(R.id.back_to_login).setOnClickListener(v -> finish());
    }

    private void handleReset() {
        String email = etEmail.getText().toString().trim();
        String newPass = etNewPass.getText().toString();
        String confirm = etConfirmPass.getText().toString();

        tilEmail.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        boolean valid = true;
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (newPass.length() < 6) {
            tilNewPass.setError("Password too short");
            valid = false;
        }
        if (!newPass.equals(confirm)) {
            tilConfirmPass.setError("Passwords do not match");
            valid = false;
        }

        if (!valid) return;

        ExpenseRepository repo = ExpenseRepository.getInstance(this);
        // We'll just force update it for this demo
        if (repo.updateUserPasswordForce(email, newPass)) {
            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
        }
    }
}
