package com.example.expensly.expensly;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensly.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private SessionManager session;
    private ExpenseRepository repository;
    private String email;

    private ShapeableImageView imgAvatar;
    private TextView tvProfileName;
    private EditText etFname, etLname;
    private EditText etCurrentPass, etNewPass, etConfirmPass;
    private TextInputLayout tilFname, tilLname;
    private TextInputLayout tilCurrentPass, tilNewPass, tilConfirmPass;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                if (AvatarStore.save(this, email, uri)) {
                    showAvatar();
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Couldn't load that image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }
        email = session.getEmail();
        repository = ExpenseRepository.getInstance(this);

        WindowUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_profile);
        WindowUtils.applyPadding(findViewById(R.id.profile_root));

        imgAvatar = findViewById(R.id.imgAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);
        etFname = findViewById(R.id.et_profile_fname);
        etLname = findViewById(R.id.et_profile_lname);
        etCurrentPass = findViewById(R.id.et_current_password);
        etNewPass = findViewById(R.id.et_change_new_password);
        etConfirmPass = findViewById(R.id.et_change_confirm_password);
        tilFname = findViewById(R.id.til_profile_fname);
        tilLname = findViewById(R.id.til_profile_lname);
        tilCurrentPass = findViewById(R.id.til_current_password);
        tilNewPass = findViewById(R.id.til_change_new_password);
        tilConfirmPass = findViewById(R.id.til_change_confirm_password);

        tvProfileEmail.setText(email);
        loadName();
        showAvatar();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> pickImage.launch("image/*"));
        imgAvatar.setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnSaveName).setOnClickListener(v -> saveName());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> changePassword());
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void loadName() {
        String[] name = repository.getUserName(email);
        if (name != null) {
            etFname.setText(name[0]);
            etLname.setText(name[1]);
            tvProfileName.setText(getString(R.string.full_name, name[0], name[1]));
        }
    }

    private void showAvatar() {
        Bitmap avatar = AvatarStore.load(this, email);
        if (avatar != null) {
            imgAvatar.setPadding(0, 0, 0, 0);
            imgAvatar.setImageTintList(null);
            imgAvatar.setImageBitmap(avatar);
        }
    }

    private void saveName() {
        tilFname.setError(null);
        tilLname.setError(null);

        String fname = etFname.getText().toString().trim();
        String lname = etLname.getText().toString().trim();

        boolean valid = true;
        if (fname.isEmpty()) {
            tilFname.setError("Required");
            valid = false;
        }
        if (lname.isEmpty()) {
            tilLname.setError("Required");
            valid = false;
        }
        if (!valid) return;

        if (repository.updateUserName(email, fname, lname)) {
            session.setFirstName(fname);
            tvProfileName.setText(getString(R.string.full_name, fname, lname));
            Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Couldn't update name", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        tilCurrentPass.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        String current = etCurrentPass.getText().toString();
        String newPass = etNewPass.getText().toString();
        String confirm = etConfirmPass.getText().toString();

        boolean valid = true;
        if (current.isEmpty()) {
            tilCurrentPass.setError("Current password is required");
            valid = false;
        }
        if (newPass.isEmpty()) {
            tilNewPass.setError("New password is required");
            valid = false;
        } else if (newPass.length() < MIN_PASSWORD_LENGTH) {
            tilNewPass.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            valid = false;
        }
        if (confirm.isEmpty()) {
            tilConfirmPass.setError("Please confirm your new password");
            valid = false;
        } else if (!newPass.isEmpty() && !newPass.equals(confirm)) {
            tilConfirmPass.setError("Passwords do not match");
            valid = false;
        }
        if (!valid) return;

        if (repository.changePassword(email, current, newPass)) {
            etCurrentPass.setText("");
            etNewPass.setText("");
            etConfirmPass.setText("");
            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
        } else {
            tilCurrentPass.setError("Current password is incorrect");
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_confirm, (dlg, which) -> {
                    session.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
