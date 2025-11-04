package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.AuthRepository;
import com.booknest.campusridenest.data.repo.ProfileRepository;
import com.booknest.campusridenest.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        EditText email = findViewById(R.id.etEmailL);
        EditText pass  = findViewById(R.id.etPassL);
        TextView status= findViewById(R.id.tvStatus);
        Button btn     = findViewById(R.id.btnLogin);

        AuthRepository authRepo = new AuthRepository();
        ProfileRepository profileRepo = ProfileRepository.getInstance();

        btn.setOnClickListener(v ->
                authRepo.signIn(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(r -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user == null) {
                                status.setText("Login failed - no user");
                                return;
                            }

                            boolean verified = authRepo.isVerified();
                            status.setText(verified ? "Verified — you can post." :
                                    "Not verified — check your email.");

                            // NEW: Check if profile exists, create if not
                            ensureProfileExists(user, profileRepo, status);
                        })
                        .addOnFailureListener(e -> status.setText(e.getMessage()))
        );
    }

    /**
     * Ensure user profile exists in Firestore
     * Create profile if it doesn't exist, then navigate to PostsActivity
     */
    private void ensureProfileExists(FirebaseUser user, ProfileRepository profileRepo, TextView status) {
        String uid = user.getUid();

        // Check if profile exists
        profileRepo.getProfile(uid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Profile exists - just navigate
                        Log.d(TAG, "Profile exists for user: " + uid);
                        navigateToPostsActivity();
                    } else {
                        // Profile doesn't exist - create it
                        Log.d(TAG, "Creating profile for user: " + uid);
                        createUserProfile(user, profileRepo, status);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking profile", e);
                    status.setText("Error checking profile");
                    // Navigate anyway - ProfileActivity will handle missing profile
                    navigateToPostsActivity();
                });
    }

    /**
     * Create new user profile in Firestore
     */
    private void createUserProfile(FirebaseUser user, ProfileRepository profileRepo, TextView status) {
        UserProfile newProfile = new UserProfile(
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : "User",
                user.getEmail() != null ? user.getEmail() : "",
                user.isEmailVerified(),
                0,  // activePosts
                0,  // closedPosts
                null  // createdAt will be set by serverTimestamp
        );

        profileRepo.createProfile(newProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile created successfully");
                    navigateToPostsActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create profile", e);
                    status.setText("Warning: Profile creation failed");
                    // Navigate anyway - can be retried later
                    navigateToPostsActivity();
                });
    }

    /**
     * Navigate to main posts activity
     */
    private void navigateToPostsActivity() {
        Intent i = new Intent(LoginActivity.this, com.booknest.campusridenest.ui.posts.PostsActivity.class);
        i.putExtra("tab", "browse");
        startActivity(i);
        finish();
    }
}
