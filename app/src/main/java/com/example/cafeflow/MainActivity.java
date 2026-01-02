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

public class MainActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private TextView roleTitleLabel;
    private LinearLayout registerLayout;
    private boolean isCustomer = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        roleTitleLabel = findViewById(R.id.roleTitleLabel);
        registerLayout = findViewById(R.id.registerLayout);

        Button btnCustomer = findViewById(R.id.btnCustomer);
        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);

        //customer clck
        btnCustomer.setOnClickListener(v -> {
            isCustomer = true;
            roleTitleLabel.setText("Customer Login");

            // highlight btn
            btnCustomer.setAlpha(1.0f);
            btnAdmin.setAlpha(0.5f);

            // visible register
            registerLayout.setVisibility(View.VISIBLE);
        });

        // admin clck
        btnAdmin.setOnClickListener(v -> {
            isCustomer = false;
            roleTitleLabel.setText("Admin Login");
            btnAdmin.setAlpha(1.0f);
            btnCustomer.setAlpha(0.5f);
            registerLayout.setVisibility(View.GONE);
        });

        // main logic
        loginButton.setOnClickListener(v -> {
            String user = usernameField.getText().toString();
            String pass = passwordField.getText().toString();
            if(user.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                //will add datbase later
                String role = isCustomer ? "Customer" : "Admin";
                Toast.makeText(this, "Logging in as " + role, Toast.LENGTH_SHORT).show();
            }
        });

        //simple navigation
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}