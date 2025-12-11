package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class RideConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_confirmation);

        // Get data from intent
        String pickupLocation = getIntent().getStringExtra("pickupLocation");
        String dropoffLocation = getIntent().getStringExtra("dropoffLocation");
        String rideTime = getIntent().getStringExtra("rideTime");
        String driverUid = getIntent().getStringExtra("driverUid");
        double pickupLat = getIntent().getDoubleExtra("pickupLat", 0);
        double pickupLon = getIntent().getDoubleExtra("pickupLon", 0);
        String offerId = getIntent().getStringExtra("offerId");

        // Initialize views
        TextView tvTitle = findViewById(R.id.tvConfirmationTitle);
        TextView tvPickup = findViewById(R.id.tvPickupLocation);
        TextView tvDropoff = findViewById(R.id.tvDropoffLocation);
        TextView tvTime = findViewById(R.id.tvRideTime);
        TextView tvDriver = findViewById(R.id.tvDriverName);
        Button btnViewMap = findViewById(R.id.btnViewOnMap);
        Button btnContactDriver = findViewById(R.id.btnContactDriver);
        Button btnDone = findViewById(R.id.btnDone);

        // Set title
        tvTitle.setText("Ride Confirmed! 🎉");

        // Display info
        tvPickup.setText("📍 Meet at: " + pickupLocation);
        tvDropoff.setText("🎯 Going to: " + dropoffLocation);
        tvTime.setText("⏰ " + rideTime);

        // Fetch driver name
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(driverUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String driverName = doc.getString("name");
                        String driverEmail = doc.getString("email");
                        tvDriver.setText("🚗 Driver: " + driverName);

                        // Contact driver button
                        btnContactDriver.setOnClickListener(v -> {
                            // Open email to contact driver
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:" + driverEmail));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Campus Ride Nest - Ride on " + rideTime);
                            startActivity(Intent.createChooser(emailIntent, "Contact Driver"));
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    tvDriver.setText("🚗 Driver: Loading...");
                });

        // View on Map button
        btnViewMap.setOnClickListener(v -> {
            String uri = "geo:" + pickupLat + "," + pickupLon + "?q=" + pickupLat + "," + pickupLon + "(" + Uri.encode(pickupLocation) + ")";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Done button
        btnDone.setOnClickListener(v -> {
            // Navigate to Active Rides or Posts
            finish();
        });
    }
}
