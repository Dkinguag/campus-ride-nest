package com.booknest.campusridenest.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.repo.ProfileRepository;
import com.booknest.campusridenest.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity - Display and edit user profiles
 *
 * Features:
 * - View mode: Shows user info, verification status, post counts
 * - Edit mode: Allows updating display name (own profile only)
 * - Email verification badge
 * - Member since date
 * - Navigation from post details or menu
 *
 * Sprint 3 US-09: User profile view
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";

    // UI Components - View Mode
    private TextView tvDisplayName;
    private TextView tvEmail;
    private TextView tvMemberSince;
    private TextView tvActivePosts;
    private TextView tvClosedPosts;
    private TextView tvTotalPosts;
    private ImageView ivVerifiedBadge;
    private Button btnEditProfile;
    private ProgressBar progressBar;

    // Repository
    private ProfileRepository profileRepository;

    // State
    private String userId;
    private boolean isOwnProfile;
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Repository
        profileRepository = ProfileRepository.getInstance();

        // Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Profile");
        }

        // Get user ID from intent
        userId = getIntent().getStringExtra(EXTRA_USER_ID);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if viewing own profile
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isOwnProfile = currentUser != null && currentUser.getUid().equals(userId);

        // Initialize views
        initializeViews();

        // Load profile data
        loadProfile();
    }

    private void initializeViews() {
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvEmail = findViewById(R.id.tv_email);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvActivePosts = findViewById(R.id.tv_active_posts);
        tvClosedPosts = findViewById(R.id.tv_closed_posts);
        tvTotalPosts = findViewById(R.id.tv_total_posts);
        ivVerifiedBadge = findViewById(R.id.iv_verified_badge);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        progressBar = findViewById(R.id.progress_bar);

        // Show edit button only for own profile
        btnEditProfile.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);

        btnEditProfile.setOnClickListener(v -> showEditDialog());
    }

    private void loadProfile() {
        showLoading(true);

        profileRepository.getProfile(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);

                    if (documentSnapshot.exists()) {
                        currentProfile = documentSnapshot.toObject(UserProfile.class);
                        if (currentProfile != null) {
                            displayProfile(currentProfile);
                        } else {
                            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        createProfileForUser();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void displayProfile(UserProfile profile) {
        // Display name
        tvDisplayName.setText(profile.getDisplayName());

        // Email
        tvEmail.setText(profile.getEmail());

        // Verification badge
        if (profile.isEmailVerified()) {
            ivVerifiedBadge.setVisibility(View.VISIBLE);
            ivVerifiedBadge.setContentDescription("Email verified");
        } else {
            ivVerifiedBadge.setVisibility(View.GONE);
        }

        // Member since
        tvMemberSince.setText(profile.getMemberSinceFormatted());

        // Post statistics
        tvActivePosts.setText(String.format("Active: %d", profile.getActivePosts()));
        tvClosedPosts.setText(String.format("Closed: %d", profile.getClosedPosts()));
        tvTotalPosts.setText(String.format("Total: %d", profile.getTotalPosts()));
    }

    /**
     * Create profile if it doesn't exist
     * This handles cases where Cloud Function didn't run
     */
    private void createProfileForUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getUid().equals(userId)) {
            UserProfile newProfile = new UserProfile(
                    userId,
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                    currentUser.getEmail() != null ? currentUser.getEmail() : "",
                    currentUser.isEmailVerified(),
                    0,
                    0,
                    null
            );

            showLoading(true);

            profileRepository.createProfile(newProfile)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        loadProfile(); // Reload to display created profile
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to create profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Show dialog to edit display name
     */
    private void showEditDialog() {
        if (currentProfile == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Inflate custom layout for edit dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etNewName = dialogView.findViewById(R.id.et_new_display_name);

        // Pre-populate with current name
        etNewName.setText(currentProfile.getDisplayName());

        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etNewName.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            updateDisplayName(newName);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void updateDisplayName(String newName) {
        showLoading(true);

        profileRepository.updateDisplayName(userId, newName)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();

                    // Update UI
                    if (currentProfile != null) {
                        currentProfile.setDisplayName(newName);
                        tvDisplayName.setText(newName);
                    }

                    // Also update Firebase Auth display name
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(newName)
                                        .build();
                        user.updateProfile(profileUpdates);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
