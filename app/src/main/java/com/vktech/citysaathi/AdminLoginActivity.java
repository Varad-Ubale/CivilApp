package com.vktech.citysaathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        emailEditText = findViewById(R.id.adminEmailEditText);
        passwordEditText = findViewById(R.id.adminPasswordEditText);
        loginButton = findViewById(R.id.adminLoginActionButton);

        loginButton.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            return;
        }

        // Check against hardcoded values
        if (email.equals("admin@citysaathi.com") && password.equals("123456789")) {
            // Credentials are correct
            Toast.makeText(AdminLoginActivity.this, "Admin login successful.", Toast.LENGTH_SHORT).show();

            // Navigate to the AdminDashboardActivity
            startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));

            finish(); // Close the admin login screen
        } else {
            // Credentials are incorrect
            Toast.makeText(AdminLoginActivity.this, "Invalid Admin Credentials.", Toast.LENGTH_SHORT).show();
        }
    }
}