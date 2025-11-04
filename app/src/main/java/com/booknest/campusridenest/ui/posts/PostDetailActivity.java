package com.booknest.campusridenest.ui.posts;

import static android.app.ProgressDialog.show;
import static android.text.format.DateUtils.formatDateTime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.repo.ProfileRepository;
import com.booknest.campusridenest.ui.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.Timestamp;

public class PostDetailActivity extends AppCompatActivity {

    private Button btnEdit, btnClosePost, btnReopenPost;
    private TextView tvOwnerName;  // NEW
    private View ownerNameLayout;  // NEW
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

        // Views
        TextView tvType = findViewById(R.id.tvType);
        TextView tvRoute = findViewById(R.id.tvRoute);
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        TextView tvSeats = findViewById(R.id.tvSeats);
        Button btnAction = findViewById(R.id.btnAction);
        btnEdit = findViewById(R.id.btn_edit_post);
        btnClosePost = findViewById(R.id.btn_close_post);
        btnReopenPost = findViewById(R.id.btn_reopen_post);

        // NEW: Owner name views
        tvOwnerName = findViewById(R.id.tvOwnerName);
        ownerNameLayout = findViewById(R.id.ownerNameLayout);

        // Show/hide buttons based on ownership and status
        setupOwnerButtons();

        // Display data with null/empty checks
        tvType.setText(type != null ? type.toUpperCase() : "POST");

        String fromText = (from != null && !from.isEmpty()) ? from : "Unknown Origin";
        String toText = (to != null && !to.isEmpty()) ? to : "Unknown Destination";
        tvRoute.setText(fromText + " → " + toText);

        tvDateTime.setText(dateTime > 0 ? formatDateTime(dateTime) : "No date specified");
        tvSeats.setText("Seats: " + seats);

        // NEW: Load owner name
        loadOwnerName();

        // Action button
        if (isOwnPost) {
            btnAction.setText("This is your post");
            btnAction.setEnabled(false);
        } else {
            if ("offer".equalsIgnoreCase(type)) {
                btnAction.setText("Request This Ride");
            } else {
                btnAction.setText("Offer a Ride");
            }

            btnAction.setOnClickListener(v -> {
                // TODO: Implement accept/respond functionality
                Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // NEW: Load and display owner name
    private void loadOwnerName() {
        if (ownerUid == null || ownerUid.isEmpty()) {
            tvOwnerName.setText("Unknown User");
            return;
        }

        ProfileRepository.getInstance()
                .getProfile(ownerUid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String displayName = documentSnapshot.getString("displayName");
                        tvOwnerName.setText(displayName != null ? displayName : "User");
                    } else {
                        tvOwnerName.setText("User");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading owner name", e);
                    tvOwnerName.setText("User");
                });

        // NEW: Make owner name clickable to open profile
        ownerNameLayout.setOnClickListener(v -> openOwnerProfile());
    }

    // NEW: Open owner's profile
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
        View ownerButtonsLayout = findViewById(R.id.ownerButtonsLayout);

        if (isOwnPost) {
            // Show the owner buttons section
            if ("open".equals(postStatus)) {
                // Show edit and close buttons
                ownerButtonsLayout.setVisibility(View.VISIBLE);
                btnReopenPost.setVisibility(View.GONE);
            } else {
                // Show reopen button
                ownerButtonsLayout.setVisibility(View.GONE);
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
            ownerButtonsLayout.setVisibility(View.GONE);
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

                    // Update local status and refresh UI
                    postStatus = "closed";
                    setupOwnerButtons();

                    // NEW: Update profile statistics (decrement active, increment closed)
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

                    // Update local status and refresh UI
                    postStatus = "open";
                    setupOwnerButtons();

                    // NEW: Update profile statistics (increment active, decrement closed)
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
            // Refresh post details or finish
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
        // Update TextViews
        TextView tvType = findViewById(R.id.tvType);
        TextView tvRoute = findViewById(R.id.tvRoute);
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        TextView tvSeats = findViewById(R.id.tvSeats);

        tvType.setText(type != null ? type.toUpperCase() : "POST");

        String fromText = (from != null && !from.isEmpty()) ? from : "Unknown Origin";
        String toText = (to != null && !to.isEmpty()) ? to : "Unknown Destination";
        tvRoute.setText(fromText + " → " + toText);

        tvDateTime.setText(dateTime > 0 ? formatDateTime(dateTime) : "No date specified");
        tvSeats.setText("Seats: " + seats);

        // Update button visibility
        setupOwnerButtons();
    }
}