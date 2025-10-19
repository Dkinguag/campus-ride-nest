package com.booknest.campusridenest.ui.posts;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.google.firebase.auth.FirebaseAuth;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get data from intent
        String postId = getIntent().getStringExtra("POST_ID");
        String type = getIntent().getStringExtra("TYPE");
        String from = getIntent().getStringExtra("FROM");
        String to = getIntent().getStringExtra("TO");
        String dateTime = getIntent().getStringExtra("DATE_TIME");
        int seats = getIntent().getIntExtra("SEATS", 0);
        String ownerUid = getIntent().getStringExtra("OWNER_UID");

        // DEBUG: Log all values
        Log.d(TAG, "POST_ID: " + postId);
        Log.d(TAG, "TYPE: " + type);
        Log.d(TAG, "FROM: " + from);
        Log.d(TAG, "TO: " + to);
        Log.d(TAG, "DATE_TIME: " + dateTime);
        Log.d(TAG, "SEATS: " + seats);
        Log.d(TAG, "OWNER_UID: " + ownerUid);

        // Initialize views
        TextView tvType = findViewById(R.id.tvType);
        TextView tvRoute = findViewById(R.id.tvRoute);
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        TextView tvSeats = findViewById(R.id.tvSeats);
        Button btnAction = findViewById(R.id.btnAction);

        // Display data with null/empty checks
        tvType.setText(type != null ? type.toUpperCase() : "POST");

        // Fix route display with better null handling
        String fromText = (from != null && !from.isEmpty()) ? from : "Unknown Origin";
        String toText = (to != null && !to.isEmpty()) ? to : "Unknown Destination";
        tvRoute.setText(fromText + " → " + toText);

        tvDateTime.setText(dateTime != null && !dateTime.isEmpty() ? dateTime : "No date specified");
        tvSeats.setText("Seats: " + seats);

        // Checking if this is the user's own post
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        boolean isOwnPost = ownerUid != null && ownerUid.equals(currentUserId);

        // Set up action button
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
}