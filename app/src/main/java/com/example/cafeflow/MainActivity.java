package com.example.cafeflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private TextView roleTitleLabel;
    private LinearLayout registerLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        roleTitleLabel = findViewById(R.id.roleTitleLabel);
        registerLayout = findViewById(R.id.registerLayout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button btnCustomer = findViewById(R.id.btnCustomer);
        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);

        //customer clck
        btnCustomer.setOnClickListener(v -> {
            roleTitleLabel.setText(getString(R.string.customer_login));
            btnCustomer.setAlpha(1.0f);
            btnAdmin.setAlpha(0.5f);
            registerLayout.setVisibility(View.VISIBLE);
        });

        // admin clck
        btnAdmin.setOnClickListener(v -> {
            roleTitleLabel.setText(getString(R.string.admin_login));
            btnAdmin.setAlpha(1.0f);
            btnCustomer.setAlpha(0.5f);
            registerLayout.setVisibility(View.GONE);
        });

        // main logic
        loginButton.setOnClickListener(v -> {
            String user = usernameField.getText().toString();
            String pass = passwordField.getText().toString();
            if(user.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, getString(R.string.fields_cannot_be_empty), Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(user, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            User currentUser = documentSnapshot.toObject(User.class);
                                            
                                            Class<?> targetActivity;
                                            if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
                                                targetActivity = AdminActivity.class;
                                            } else {
                                                targetActivity = CustomerActivity.class;
                                            }
                                            Intent intent = new Intent(MainActivity.this, targetActivity);
                                            startActivity(intent);

                                        } else {
                                            Toast.makeText(MainActivity.this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(MainActivity.this, getString(R.string.login_failed, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });

        //simple navigation
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}