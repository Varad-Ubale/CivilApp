package com.vktech.citysaathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        nameEditText = findViewById(R.id.registerNameEditText);
        emailEditText = findViewById(R.id.registerEmailEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // --- Input Validation ---
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required.");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match.");
            return;
        }

        registerButton.setEnabled(false); // Disable button to prevent multiple clicks

        // --- Create User with Firebase ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Update user profile with their name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();

                        if (user != null) {
                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }
                            });
                        }
                        Toast.makeText(RegisterActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                        // Navigate back to LoginActivity
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed. " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true); // Re-enable button on failure
                    }
                });
    }
}