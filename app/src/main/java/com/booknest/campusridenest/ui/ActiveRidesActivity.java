package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.ui.adapters.ActiveRidesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActiveRidesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActiveRidesAdapter adapter;
    private LinearLayout emptyState;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_rides);

        // NEW: Setup toolbar with back button
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerViewActiveRides);
        emptyState = findViewById(R.id.emptyStateActive);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveRidesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadActiveRides();
    }

    private void loadActiveRides() {
        // Listen for your active offers that have riders
        db.collection("offers")
                .whereEqualTo("ownerUid", currentUserId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ActiveRidesActivity", "Error loading offers", error);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    List<ActiveRide> activeRides = new ArrayList<>();

                    for (DocumentSnapshot offerDoc : snapshots.getDocuments()) {
                        String offerId = offerDoc.getId();
                        String from = offerDoc.getString("from");
                        String to = offerDoc.getString("to");

                        // FIX: Handle different time field types
                        Long timeMillis = null;
                        try {
                            // Try getting as Long first
                            timeMillis = offerDoc.getLong("dateTime");
                        } catch (Exception e1) {
                            try {
                                // Try alternate field name
                                timeMillis = offerDoc.getLong("timeMillis");
                            } catch (Exception e2) {
                                // Try getting as Timestamp
                                com.google.firebase.Timestamp timestamp = offerDoc.getTimestamp("dateTime");
                                if (timestamp != null) {
                                    timeMillis = timestamp.toDate().getTime();
                                } else {
                                    timestamp = offerDoc.getTimestamp("timeMillis");
                                    if (timestamp != null) {
                                        timeMillis = timestamp.toDate().getTime();
                                    }
                                }
                            }
                        }

                        // If still null, use current time as fallback
                        if (timeMillis == null) {
                            timeMillis = System.currentTimeMillis();
                            Log.w("ActiveRidesActivity", "Could not get timeMillis for offer " + offerId);
                        }

                        final Long finalTimeMillis = timeMillis;

                        // Check for riders in this offer
                        db.collection("offers")
                                .document(offerId)
                                .collection("riders")
                                .addSnapshotListener((riderSnapshots, e) -> {
                                    if (e != null) {
                                        Log.e("ActiveRidesActivity", "Error loading riders", e);
                                        return;
                                    }

                                    if (riderSnapshots == null || riderSnapshots.isEmpty()) {
                                        return;
                                    }

                                    for (DocumentSnapshot riderDoc : riderSnapshots.getDocuments()) {
                                        String riderId = riderDoc.getString("userId");
                                        String requestId = riderDoc.getString("requestId");

                                        if (riderId == null || requestId == null) {
                                            continue;
                                        }

                                        // Fetch request to get pickup location
                                        db.collection("requests")
                                                .document(requestId)
                                                .get()
                                                .addOnSuccessListener(requestDoc -> {
                                                    if (!requestDoc.exists()) return;

                                                    String pickupLocation = requestDoc.getString("from");

                                                    // Fetch rider info
                                                    db.collection("users")
                                                            .document(riderId)
                                                            .get()
                                                            .addOnSuccessListener(userDoc -> {
                                                                if (!userDoc.exists()) return;

                                                                String riderName = userDoc.getString("name");
                                                                if (riderName == null) {
                                                                    riderName = "Unknown Rider";
                                                                }

                                                                ActiveRide ride = new ActiveRide();
                                                                ride.offerId = offerId;
                                                                ride.from = from;
                                                                ride.to = to;
                                                                ride.timeMillis = finalTimeMillis;
                                                                ride.riderName = riderName;
                                                                ride.pickupLocation = pickupLocation != null ? pickupLocation : "Unknown";
                                                                ride.riderId = riderId;
                                                                ride.requestId = requestId;

                                                                // Add to list if not already present
                                                                boolean alreadyExists = false;
                                                                for (ActiveRide existing : activeRides) {
                                                                    if (existing.riderId.equals(riderId) &&
                                                                            existing.offerId.equals(offerId)) {
                                                                        alreadyExists = true;
                                                                        break;
                                                                    }
                                                                }

                                                                if (!alreadyExists) {
                                                                    activeRides.add(ride);
                                                                    updateUI(activeRides);
                                                                }
                                                            })
                                                            .addOnFailureListener(err -> {
                                                                Log.e("ActiveRidesActivity", "Error fetching user", err);
                                                            });
                                                })
                                                .addOnFailureListener(err -> {
                                                    Log.e("ActiveRidesActivity", "Error fetching request", err);
                                                });
                                    }
                                });
                    }
                });
    }

    private void updateUI(List<ActiveRide> rides) {
        if (rides.isEmpty()) {
            showEmptyState();
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateRides(rides);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    // Inner class to hold active ride data
    public static class ActiveRide {
        public String offerId;
        public String from;
        public String to;
        public Long timeMillis;
        public String riderName;
        public String pickupLocation;
        public String riderId;
        public String requestId;
    }
}