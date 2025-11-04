package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.MainActivity;
import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.AuthRepository;
import com.booknest.campusridenest.data.repo.ProfileRepository;
import com.booknest.campusridenest.model.UserProfile;
import com.booknest.campusridenest.util.ValidationResult;
import com.booknest.campusridenest.util.ValidationRules;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText pass;
    private Button btn;
    private AuthRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.etEmailR);
        pass = findViewById(R.id.etPassR);
        btn = findViewById(R.id.btnRegister);
        repo = new AuthRepository();

        btn.setOnClickListener(v -> {
            // Get form field values FIRST
            String emailText = email.getText().toString().trim();
            String password = pass.getText().toString().trim();
            String displayName = emailText.split("@")[0]; // Extract name from email

            // Validate inputs
            if (emailText.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ValidationResult emailValidation = ValidationRules.validateEduEmail(emailText);
            if (emailValidation instanceof ValidationResult.Invalid) {
                Toast.makeText(this, ((ValidationResult.Invalid) emailValidation).getError(), Toast.LENGTH_SHORT).show();
                return;
            }

            ValidationResult passwordValidation = ValidationRules.validatePassword(password);
            if (passwordValidation instanceof ValidationResult.Invalid) {
                Toast.makeText(this, ((ValidationResult.Invalid) passwordValidation).getError(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Single signup flow - use repository signup which handles everything
            repo.signUp(emailText, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();

                        if (user != null) {
                            // Create user profile in Firestore
                            UserProfile profile = new UserProfile(
                                    user.getUid(),
                                    displayName,
                                    emailText,
                                    user.isEmailVerified(),
                                    0,
                                    0,
                                    null
                            );

                            ProfileRepository.getInstance().createProfile(profile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Check your email to verify.", Toast.LENGTH_LONG).show();
                                        // Profile created, proceed to main app
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to create profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}