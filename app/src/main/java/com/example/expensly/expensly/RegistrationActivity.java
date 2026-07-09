package com.example.expensly.expensly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText mEmail;
    private EditText mPass;
    private EditText rmPass;
    private EditText fname;
    private EditText lName;

    private TextInputLayout tilFname;
    private TextInputLayout tilLname;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPass;
    private TextInputLayout tilRePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        WindowUtils.applyEdgeToEdge(this, findViewById(R.id.main));
        setupViews();
    }

    private void setupViews() {
        fname = findViewById(R.id.fname_reg);
        lName = findViewById(R.id.lname_reg);
        mEmail = findViewById(R.id.email_reg);
        mPass = findViewById(R.id.password_reg);
        rmPass = findViewById(R.id.repassword_reg);

        tilFname = findViewById(R.id.til_fname_reg);
        tilLname = findViewById(R.id.til_lname_reg);
        tilEmail = findViewById(R.id.til_email_reg);
        tilPass = findViewById(R.id.til_password_reg);
        tilRePass = findViewById(R.id.til_repassword_reg);

        Button btnReg = findViewById(R.id.reg_btn_login);
        TextView tvSignin = findViewById(R.id.alr_acc);

        tvSignin.setOnClickListener(v -> finish());
        btnReg.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String fName = fname.getText().toString().trim();
        String lNameStr = lName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPass.getText().toString();
        String rePassword = rmPass.getText().toString();

        if (!validate(fName, lNameStr, email, password, rePassword)) return;

        boolean success = ExpenseRepository.getInstance(this)
                .registerUser(fName, lNameStr, email, password);

        if (success) {
            // Log the new user straight in — no need to type it all again.
            SessionManager session = new SessionManager(this);
            session.login(email.toLowerCase(Locale.ROOT), fName);
            Toast.makeText(this, "Welcome, " + fName + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            tilEmail.setError("An account with this email already exists");
        }
    }

    private boolean validate(String fName, String lNameStr, String email,
                             String password, String rePassword) {
        tilFname.setError(null);
        tilLname.setError(null);
        tilEmail.setError(null);
        tilPass.setError(null);
        tilRePass.setError(null);

        boolean valid = true;

        if (fName.isEmpty()) {
            tilFname.setError("Required");
            valid = false;
        }
        if (lNameStr.isEmpty()) {
            tilLname.setError("Required");
            valid = false;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPass.setError("Password is required");
            valid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPass.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            valid = false;
        }
        if (rePassword.isEmpty()) {
            tilRePass.setError("Please confirm your password");
            valid = false;
        } else if (!password.isEmpty() && !password.equals(rePassword)) {
            tilRePass.setError("Passwords do not match");
            valid = false;
        }
        return valid;
    }
}
