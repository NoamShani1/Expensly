package com.example.expensly.expense_tracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.expensly.R;

public class Login extends AppCompatActivity {

    private EditText nEmail;
    private EditText nPassword;
    private Button btnEmail;
    private TextView forgotPw;
    private TextView nSignUp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void login(){
        nEmail = findViewById(R.id.email_login);
        nPassword = findViewById(R.id.password_login);
        btnEmail = findViewById(R.id.btn_login);
        forgotPw = findViewById(R.id.forgot_password);
        nSignUp = findViewById(R.id.signup);
    }

}