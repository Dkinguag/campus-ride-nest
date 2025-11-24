package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.ui.posts.PostsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_login);
            Log.d(TAG, "LoginActivity onCreate");

            // Initialize Firebase Auth with error handling
            try {
                mAuth = FirebaseAuth.getInstance();
                Log.d(TAG, "Firebase Auth initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Firebase Auth: " + e.getMessage(), e);
                Toast.makeText(this, "Authentication service unavailable", Toast.LENGTH_LONG).show();
            }

            // Initialize views
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvRegister = findViewById(R.id.tvRegister);
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            progressBar = findViewById(R.id.progressBar);

            // Login button click
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptLogin();
                }
            });

            // Register link click
            tvRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to open RegisterActivity: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Cannot open registration", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Forgot password link click
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleForgotPassword();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        try {
            Log.d(TAG, "Login attempt started");

            // Check if Firebase is initialized
            if (mAuth == null) {
                Log.e(TAG, "Firebase Auth is null - cannot login");
                Toast.makeText(this, "Authentication service not available", Toast.LENGTH_LONG).show();
                return;
            }

            // Get email and password
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email");
                etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            Log.d(TAG, "Validation passed, attempting Firebase sign in");

            // Show progress
            showLoading(true);

            // Firebase authentication with comprehensive error handling
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            // Login success
                            Log.d(TAG, "Login successful");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                Log.d(TAG, "User authenticated: " + user.getEmail());
                                Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();

                                try {
                                    // Navigate to PostsActivity
                                    Intent intent = new Intent(LoginActivity.this, PostsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to navigate to PostsActivity: " + e.getMessage(), e);
                                    Toast.makeText(LoginActivity.this, "Navigation error", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "User is null after successful login");
                                Toast.makeText(LoginActivity.this, "Login succeeded but user info unavailable", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Login failed
                            String errorMessage;
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                                Log.e(TAG, "Login failed: " + errorMessage, task.getException());
                            } else {
                                errorMessage = "Authentication failed";
                                Log.e(TAG, "Login failed with no exception");
                            }

                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Log.e(TAG, "Login failure: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Exception during login attempt: " + e.getMessage(), e);
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleForgotPassword() {
        try {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                etEmail.requestFocus();
                return;
            }

            if (mAuth == null) {
                Toast.makeText(this, "Authentication service not available", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Failed to send reset email";
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Exception in forgot password: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (btnLogin != null) {
                btnLogin.setEnabled(!show);
            }
            if (etEmail != null) {
                etEmail.setEnabled(!show);
            }
            if (etPassword != null) {
                etPassword.setEnabled(!show);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showLoading: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // Check if user is already logged in
            if (mAuth != null) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    Log.d(TAG, "User already logged in, going to posts");
                    startActivity(new Intent(this, PostsActivity.class));
                    finish();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in onStart: " + e.getMessage(), e);
            // Don't crash - just stay on login screen
        }
    }
}