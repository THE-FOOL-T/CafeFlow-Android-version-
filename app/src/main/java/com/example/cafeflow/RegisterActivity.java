package com.example.cafeflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameField, emailField, passwordField, confirmPasswordField;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameField = findViewById(R.id.fullNameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.regPasswordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        Button registerButton = findViewById(R.id.registerButton);
        TextView backToLogin = findViewById(R.id.backToLoginLink);

        registerButton.setOnClickListener(v -> {
            String name = fullNameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String pass = passwordField.getText().toString().trim();
            String confirmPass = confirmPasswordField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(RegisterActivity.this, getString(R.string.fields_cannot_be_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirmPass)) {
                Toast.makeText(RegisterActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            // Create User in Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //  Save Extra Details (Name, Role) to Firestore
                            String uid = mAuth.getCurrentUser().getUid();
                            User newUser = new User(uid, name, email, Role.CUSTOMER);

                            db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
                                        finish(); // Go back to Login
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, getString(R.string.failed_to_save_user_data), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.registration_failed, task.getException().getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}