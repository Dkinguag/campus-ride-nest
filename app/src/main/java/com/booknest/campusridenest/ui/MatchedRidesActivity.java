package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.model.RideMatch;
import com.booknest.campusridenest.model.RideRequest;
import com.booknest.campusridenest.services.RideMatchingService;
import com.booknest.campusridenest.ui.adapters.MatchedRidesAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MatchedRidesActivity extends AppCompatActivity implements MatchedRidesAdapter.OnMatchClickListener {

    private RecyclerView recyclerView;
    private MatchedRidesAdapter adapter;
    private RideMatchingService matchingService;
    private RideRequest currentRequest;

    private LinearLayout loadingLayout;
    private LinearLayout emptyLayout;
    private LinearLayout errorLayout;
    private TextView tvRequestInfo;
    private TextView tvErrorMessage;
    private Button btnRetry;

    // NEW: Firestore and Auth
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar checkoutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_rides);

        // NEW: Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewMatches);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        errorLayout = findViewById(R.id.errorLayout);
        tvRequestInfo = findViewById(R.id.tvRequestInfo);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        matchingService = new RideMatchingService();

        // Reconstructed RideRequest from individual extras
        Intent intent = getIntent();

        String requestId = intent.getStringExtra("request_id");
        String from = intent.getStringExtra("from");
        String to = intent.getStringExtra("to");
        long timeMillis = intent.getLongExtra("timeMillis", 0);
        int seats = intent.getIntExtra("seats", 1);
        double maxBudget = intent.getDoubleExtra("maxBudget", 0.0);

        double pickupLat = intent.getDoubleExtra("pickupLat", 0);
        double pickupLon = intent.getDoubleExtra("pickupLon", 0);
        double dropoffLat = intent.getDoubleExtra("dropoffLat", 0);
        double dropoffLon = intent.getDoubleExtra("dropoffLon", 0);

        boolean needsNonSmoking = intent.getBooleanExtra("needsNonSmoking", false);
        boolean needsNoPets = intent.getBooleanExtra("needsNoPets", false);
        String musicPref = intent.getStringExtra("musicPreference");
        String conversationPref = intent.getStringExtra("conversationLevel");
        String ownerUid = intent.getStringExtra("ownerUid");

        // Reconstructed RideRequest
        currentRequest = new RideRequest();
        currentRequest.id = requestId;
        currentRequest.ownerUid = ownerUid;
        currentRequest.from = from;
        currentRequest.to = to;
        currentRequest.origin = from;
        currentRequest.destination = to;
        currentRequest.timeMillis = timeMillis;
        currentRequest.seats = seats;
        currentRequest.pickupLocation = new GeoPoint(pickupLat, pickupLon);
        currentRequest.dropoffLocation = new GeoPoint(dropoffLat, dropoffLon);
        currentRequest.needsNonSmoking = needsNonSmoking;
        currentRequest.needsNoPets = needsNoPets;
        currentRequest.musicPreference = musicPref;
        currentRequest.conversationLevel = conversationPref;
        currentRequest.maxBudget = maxBudget;

        if (currentRequest.from == null || currentRequest.to == null) {
            Toast.makeText(this, "Error: Invalid request data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display request info
        displayRequestInfo();

        // Retry button
        btnRetry.setOnClickListener(v -> findMatches());

        // Start finding matches
        findMatches();
    }

    private void displayRequestInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        String dateStr = sdf.format(new Date(currentRequest.timeMillis));

        String info = currentRequest.from + " → " + currentRequest.to + "\n" + dateStr;
        tvRequestInfo.setText(info);
    }

    private void findMatches() {
        showLoading(true);

        matchingService.findMatchesForRequest(currentRequest, new RideMatchingService.MatchCallback() {
            @Override
            public void onMatchesFound(List<RideMatch> matches) {
                showLoading(false);

                if (matches.isEmpty()) {
                    showEmpty(true);
                } else {
                    showEmpty(false);
                    // UPDATED: Pass 'this' as the click listener
                    adapter = new MatchedRidesAdapter(matches, MatchedRidesActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                showError(error);
            }
        });
    }

    // NEW: Handle match click from adapter
    @Override
    public void onMatchClick(RideMatch match) {
        showCheckoutConfirmation(match);
    }

    // NEW: Show confirmation dialog
    private void showCheckoutConfirmation(RideMatch match) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault());
        String dateStr = sdf.format(new Date(match.offer.timeMillis));

        String message = String.format(Locale.getDefault(),
                "Join this ride?\n\n" +
                        "Route: %s → %s\n" +
                        "Date: %s\n" +
                        "Price: $%.2f per seat\n" +
                        "Match Score: %d%%",
                match.offer.from,
                match.offer.to,
                dateStr,
                match.offer.pricePerSeat,
                (int) match.matchScore);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Ride Selection")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    processCheckout(match);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // NEW: Process checkout in Firestore
    private void processCheckout(RideMatch match) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String offerId = match.offer.id;
        String requestId = currentRequest.id;

        // Transaction to ensure atomic updates
        db.runTransaction(transaction -> {
            // 1. Get current offer data
            var offerRef = db.collection("offers").document(offerId);
            var offerSnapshot = transaction.get(offerRef);

            if (!offerSnapshot.exists()) {
                throw new RuntimeException("Offer no longer exists");
            }

            Long currentSeats = offerSnapshot.getLong("seats");
            if (currentSeats == null || currentSeats <= 0) {
                throw new RuntimeException("No seats available");
            }

            // 2. Update offer: decrement seats, add rider
            int newSeats = currentSeats.intValue() - 1;
            Map<String, Object> offerUpdates = new HashMap<>();
            offerUpdates.put("seats", newSeats);
            offerUpdates.put("updatedAt", FieldValue.serverTimestamp());

            // If no seats left, mark as full
            if (newSeats <= 0) {
                offerUpdates.put("status", "full");
            }

            transaction.update(offerRef, offerUpdates);

            // 3. Add rider to offer's riders subcollection (optional)
            var riderRef = offerRef.collection("riders").document(currentUserId);
            Map<String, Object> riderData = new HashMap<>();
            riderData.put("userId", currentUserId);
            riderData.put("requestId", requestId);
            riderData.put("joinedAt", FieldValue.serverTimestamp());
            riderData.put("seatsBooked", 1);
            transaction.set(riderRef, riderData);

            // 4. Update request status to "matched"
            if (requestId != null && !requestId.isEmpty()) {
                var requestRef = db.collection("requests").document(requestId);
                Map<String, Object> requestUpdates = new HashMap<>();
                requestUpdates.put("status", "matched");
                requestUpdates.put("matchedOfferId", offerId);
                requestUpdates.put("updatedAt", FieldValue.serverTimestamp());
                transaction.update(requestRef, requestUpdates);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            showLoading(false);
            showCheckoutSuccess(match);
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Checkout failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // NEW: Show success and navigate
    private void showCheckoutSuccess(RideMatch match) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ride Confirmed! 🎉")
                .setMessage("You've successfully joined the ride from " +
                        match.offer.from + " to " + match.offer.to +
                        ".\n\nThe driver will be notified of your booking.")
                .setPositiveButton("Done", (dialog, which) -> {
                    // Navigate back to posts
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    private void showEmpty(boolean show) {
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    private void showError(String message) {
        errorLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
    }
}