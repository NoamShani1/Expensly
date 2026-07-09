package com.example.expensly.expense_tracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.expensly.R;

public class RegistrationActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private EditText rmPass;
    private EditText fname;
    private EditText lName;
    private Button btn_reg;
    private TextView mSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            registration();
            return insets;
        });
    }

    private void registration(){
        fname = findViewById(R.id.fname_reg);
        lName = findViewById(R.id.lname_reg);
        mEmail = findViewById(R.id.email_reg);
        mPass = findViewById(R.id.password_reg);
        rmPass = findViewById(R.id.repassword_reg);
        btn_reg = findViewById(R.id.reg_btn_login);
        mSignin = findViewById(R.id.alr_acc);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f_name = fname.getText().toString().trim();
                String l_name= lName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email required");
                }
                if(TextUtils.isEmpty(f_name)){
                    fname.setError("Field required");
                }
                if(TextUtils.isEmpty(l_name)){
                    lName.setError("Field required");
                }

                validatePasswords();

            }
        });
    }

    //validating pass
    private boolean validatePasswords() {
        String password = mPass.getText().toString().trim();
        String rePassword = rmPass.getText().toString().trim();
        if(TextUtils.isEmpty(password)){
            mPass.setError("Password Required");
        }
        if(TextUtils.isEmpty(rePassword)){
            rmPass.setError("Password Required");
        }
        if (!password.equals(rePassword)) {
            rmPass.setError("Passwords do not match");
            rmPass.requestFocus();
            Toast.makeText(this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


}