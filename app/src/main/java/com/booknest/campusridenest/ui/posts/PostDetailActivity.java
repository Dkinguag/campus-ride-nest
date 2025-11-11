package com.booknest.campusridenest.ui.posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.repo.ProfileRepository;
import com.booknest.campusridenest.ui.profile.ProfileActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    // FIXED: Updated button variables to match XML IDs
    private MaterialButton btnEdit, btnClosePost, btnReopenPost;
    private TextView tvOwnerName;
    private LinearLayout ownerLayout;
    private static final int REQUEST_EDIT_POST = 100;

    // Post data variables
    private String type;
    private String postId;
    private String from;
    private String to;
    private long dateTime;
    private int seats;
    private int price;
    private String ownerUid;
    private String postStatus;
    private boolean isOwnPost;

    private static final String TAG = "PostDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get data from Intent
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        postId = intent.getStringExtra("postId");
        from = intent.getStringExtra("from");
        to = intent.getStringExtra("to");
        dateTime = intent.getLongExtra("dateTime", 0);
        seats = intent.getIntExtra("seats", 0);
        price = intent.getIntExtra("price", 0);
        ownerUid = intent.getStringExtra("ownerUid");
        postStatus = intent.getStringExtra("status");
        if (postStatus == null) postStatus = "open";

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        isOwnPost = currentUserId != null && currentUserId.equals(ownerUid);

        // FIXED: Views with correct IDs from XML
        TextView statusBadge = findViewById(R.id.statusBadge);
        TextView routeText = findViewById(R.id.routeText);
        TextView dateTimeText = findViewById(R.id.dateTimeText);
        TextView seatsText = findViewById(R.id.seatsText);
        LinearLayout priceLayout = findViewById(R.id.priceLayout);
        TextView priceText = findViewById(R.id.priceText);

        btnEdit = findViewById(R.id.editButton);
        btnClosePost = findViewById(R.id.closeButton);
        btnReopenPost = findViewById(R.id.reopenButton);

        // Owner info views
        tvOwnerName = findViewById(R.id.ownerName);
        ownerLayout = findViewById(R.id.ownerLayout);
        TextView ownerEmail = findViewById(R.id.ownerEmail);
        ImageView verificationBadge = findViewById(R.id.verificationBadge);

        // Show/hide buttons based on ownership and status
        setupOwnerButtons();

        // Display data with null/empty checks
        statusBadge.setText(type != null ? type.toUpperCase() : "POST");

        // Set badge color based on type
        if ("offer".equalsIgnoreCase(type)) {
            statusBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            statusBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        String fromText = (from != null && !from.isEmpty()) ? from : "Unknown Origin";
        String toText = (to != null && !to.isEmpty()) ? to : "Unknown Destination";
        routeText.setText(fromText + " → " + toText);

        dateTimeText.setText(dateTime > 0 ? formatDateTime(dateTime) : "No date specified");
        seatsText.setText(seats + " seats");

        // Show price layout only for offers
        if ("offer".equalsIgnoreCase(type) && price > 0) {
            priceLayout.setVisibility(View.VISIBLE);
            priceText.setText("$" + price + " per seat");
        } else {
            priceLayout.setVisibility(View.GONE);
        }

        // Load owner information
        loadOwnerInfo(ownerEmail, verificationBadge);

        // Make owner layout clickable
        ownerLayout.setOnClickListener(v -> openOwnerProfile());

        // Back button - FIXED: correct ID
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    // Load owner name, email, and verification status
    private void loadOwnerInfo(TextView ownerEmail, ImageView verificationBadge) {
        if (ownerUid == null || ownerUid.isEmpty()) {
            tvOwnerName.setText("Unknown User");
            ownerEmail.setText("");
            verificationBadge.setVisibility(View.GONE);
            return;
        }

        ProfileRepository.getInstance()
                .getProfile(ownerUid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Display name
                        String displayName = documentSnapshot.getString("displayName");
                        tvOwnerName.setText(displayName != null ? displayName : "User");

                        // ACCESSIBILITY: Set dynamic content description for owner layout
                        ownerLayout.setContentDescription(
                                getString(R.string.cd_owner_name, displayName != null ? displayName : "User")
                        );

                        // Email
                        String email = documentSnapshot.getString("email");
                        ownerEmail.setText(email != null ? email : "");

                        // Verification badge
                        Boolean isVerified = documentSnapshot.getBoolean("isVerified");
                        if (isVerified != null && isVerified) {
                            verificationBadge.setVisibility(View.VISIBLE);
                            verificationBadge.setContentDescription(getString(R.string.cd_verification_badge_verified));
                        } else {
                            verificationBadge.setVisibility(View.GONE);
                        }
                    } else {
                        tvOwnerName.setText("User");
                        ownerEmail.setText("");
                        verificationBadge.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading owner info", e);
                    tvOwnerName.setText("User");
                    ownerEmail.setText("");
                    verificationBadge.setVisibility(View.GONE);
                });
    }

    // Open owner's profile
    private void openOwnerProfile() {
        if (ownerUid == null || ownerUid.isEmpty()) {
            Toast.makeText(this, "Cannot load profile", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, ownerUid);
        startActivity(intent);
    }

    private String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void setupOwnerButtons() {
        // FIXED: Use correct XML ID
        LinearLayout actionButtonsLayout = findViewById(R.id.actionButtonsLayout);

        if (isOwnPost) {
            // Show appropriate buttons based on status
            if ("open".equals(postStatus)) {
                // Show edit and close buttons
                actionButtonsLayout.setVisibility(View.VISIBLE);
                btnReopenPost.setVisibility(View.GONE);
            } else {
                // Show reopen button
                actionButtonsLayout.setVisibility(View.GONE);
                btnReopenPost.setVisibility(View.VISIBLE);
            }

            // Edit button click
            btnEdit.setOnClickListener(v -> launchEditActivity());

            // Close button click
            btnClosePost.setOnClickListener(v -> showCloseConfirmation());

            // Reopen button click
            btnReopenPost.setOnClickListener(v -> reopenPost());
        } else {
            // Not the owner - hide all owner buttons
            actionButtonsLayout.setVisibility(View.GONE);
            btnReopenPost.setVisibility(View.GONE);
        }
    }

    private void launchEditActivity() {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra(EditPostActivity.EXTRA_POST_TYPE, type);
        intent.putExtra(EditPostActivity.EXTRA_POST_ID, postId);
        intent.putExtra(EditPostActivity.EXTRA_FROM, from);
        intent.putExtra(EditPostActivity.EXTRA_TO, to);
        intent.putExtra(EditPostActivity.EXTRA_DATE_TIME, dateTime);
        intent.putExtra(EditPostActivity.EXTRA_SEATS, seats);
        if ("offer".equalsIgnoreCase(type)) {
            intent.putExtra(EditPostActivity.EXTRA_PRICE, price);
        }
        startActivityForResult(intent, REQUEST_EDIT_POST);
    }

    private void showCloseConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Close Post")
                .setMessage("Are you sure you want to close this post? It will no longer appear in Browse for other users.")
                .setPositiveButton("Close Post", (dialog, which) -> closePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void closePost() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "closed");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection(type.equalsIgnoreCase("offer") ? "offers" : "requests")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post closed successfully", Toast.LENGTH_SHORT).show();

                    // ACCESSIBILITY: Announce post closed
                    announcePostClosed();

                    // Update local status and refresh UI
                    postStatus = "closed";
                    setupOwnerButtons();

                    // Update profile statistics
                    ProfileRepository.getInstance()
                            .closePost(ownerUid)
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update profile stats", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to close post", Toast.LENGTH_SHORT).show();
                });
    }

    private void reopenPost() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "open");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection(type.equalsIgnoreCase("offer") ? "offers" : "requests")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post reopened successfully", Toast.LENGTH_SHORT).show();

                    // ACCESSIBILITY: Announce post reopened
                    announcePostReopened();

                    // Update local status and refresh UI
                    postStatus = "open";
                    setupOwnerButtons();

                    // Update profile statistics (increment active, decrement closed)
                    Map<String, Object> profileUpdates = new HashMap<>();
                    profileUpdates.put("activePosts", FieldValue.increment(1));
                    profileUpdates.put("closedPosts", FieldValue.increment(-1));

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(ownerUid)
                            .update(profileUpdates)
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update profile stats", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to reopen post", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_POST && resultCode == RESULT_OK) {
            // Refresh post details
            reloadPostData();
        }
    }

    private void reloadPostData() {
        if (postId == null || type == null) {
            finish();
            return;
        }

        String collection = type.equalsIgnoreCase("offer") ? "offers" : "requests";

        FirebaseFirestore.getInstance()
                .collection(collection)
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Update the data variables
                        from = documentSnapshot.getString("from");
                        to = documentSnapshot.getString("to");

                        // Handle timestamp
                        Object dateTimeObj = documentSnapshot.get("dateTime");
                        if (dateTimeObj instanceof com.google.firebase.Timestamp) {
                            dateTime = ((com.google.firebase.Timestamp) dateTimeObj).getSeconds() * 1000;
                        } else if (dateTimeObj instanceof Long) {
                            dateTime = (Long) dateTimeObj;
                        }

                        Long seatsLong = documentSnapshot.getLong("seats");
                        seats = seatsLong != null ? seatsLong.intValue() : 0;

                        if (type.equalsIgnoreCase("offer")) {
                            Long priceLong = documentSnapshot.getLong("price");
                            price = priceLong != null ? priceLong.intValue() : 0;
                        }

                        postStatus = documentSnapshot.getString("status");
                        if (postStatus == null) postStatus = "open";

                        // Refresh the UI
                        refreshUI();

                        // ACCESSIBILITY: Announce post updated (if this was called after edit)
                        announcePostUpdated();
                    } else {
                        Toast.makeText(this, "Post no longer exists", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to reload post", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error reloading post", e);
                });
    }

    private void refreshUI() {
        // FIXED: Update TextViews using correct IDs
        TextView statusBadge = findViewById(R.id.statusBadge);
        TextView routeText = findViewById(R.id.routeText);
        TextView dateTimeText = findViewById(R.id.dateTimeText);
        TextView seatsText = findViewById(R.id.seatsText);
        LinearLayout priceLayout = findViewById(R.id.priceLayout);
        TextView priceText = findViewById(R.id.priceText);

        statusBadge.setText(type != null ? type.toUpperCase() : "POST");

        String fromText = (from != null && !from.isEmpty()) ? from : "Unknown Origin";
        String toText = (to != null && !to.isEmpty()) ? to : "Unknown Destination";
        routeText.setText(fromText + " → " + toText);

        dateTimeText.setText(dateTime > 0 ? formatDateTime(dateTime) : "No date specified");
        seatsText.setText(seats + " seats");

        // Update price visibility
        if ("offer".equalsIgnoreCase(type) && price > 0) {
            priceLayout.setVisibility(View.VISIBLE);
            priceText.setText("$" + price + " per seat");
        } else {
            priceLayout.setVisibility(View.GONE);
        }

        // Update button visibility
        setupOwnerButtons();
    }
    // ============ ACCESSIBILITY ANNOUNCEMENTS (Commit 4) ============

    /**
     * Announce when post is closed
     * Waits briefly to ensure announcement completes
     */
    private void announcePostClosed() {
        String announcement = getString(R.string.announce_post_closed);
        findViewById(android.R.id.content).announceForAccessibility(announcement);
    }

    /**
     * Announce when post is reopened
     */
    private void announcePostReopened() {
        String announcement = getString(R.string.announce_post_reopened);
        findViewById(android.R.id.content).announceForAccessibility(announcement);
    }

    /**
     * Announce when post is updated after editing
     * Need to add this string to strings.xml
     */
    private void announcePostUpdated() {
        String announcement = "Post updated successfully";
        findViewById(android.R.id.content).announceForAccessibility(announcement);
    }
}