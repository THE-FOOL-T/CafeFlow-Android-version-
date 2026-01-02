package com.example.cafeflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameField, emailField, passwordField, confirmPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullNameField = findViewById(R.id.fullNameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.regPasswordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        Button registerButton = findViewById(R.id.registerButton);
        TextView backToLogin = findViewById(R.id.backToLoginLink);

        registerButton.setOnClickListener(v -> {
            String name = fullNameField.getText().toString();
            String email = emailField.getText().toString();
            String pass = passwordField.getText().toString();
            String confirmPass = confirmPasswordField.getText().toString();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            // will integate database later

            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}