package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.util.GeocodingService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OfferCreateActivity extends AppCompatActivity {

    private EditText etFrom, etTo, etSeats, etPrice;
    private Button btnPickDate, btnPickTime, btnSubmit;
    private ProgressBar progressBar;

    // NEW: Preference UI elements
    private CheckBox cbAllowSmoking, cbAllowPets;
    private RadioGroup rgMusic, rgConversation;

    private FirebaseFirestore db;
    private GeocodingService geocodingService;

    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_create);

        db = FirebaseFirestore.getInstance();
        geocodingService = new GeocodingService(this);
        selectedDateTime = Calendar.getInstance();

        // Initialize views
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etSeats = findViewById(R.id.etSeats);
        etPrice = findViewById(R.id.etPrice);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // NEW: Initialize preference views
        cbAllowSmoking = findViewById(R.id.cbAllowSmoking);
        cbAllowPets = findViewById(R.id.cbAllowPets);
        rgMusic = findViewById(R.id.rgMusic);
        rgConversation = findViewById(R.id.rgConversation);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void showDatePicker() {
        new android.app.DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new android.app.TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void updateDateTimeDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault());
        String dateTimeStr = sdf.format(selectedDateTime.getTime());
        btnPickDate.setText(dateTimeStr);
    }

    private void handleSubmit() {
        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String seatsStr = etSeats.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // Validation
        if (from.isEmpty() || to.isEmpty() || seatsStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats = Integer.parseInt(seatsStr);
        double price = Double.parseDouble(priceStr);

        // Get preference values
        boolean allowsSmoking = cbAllowSmoking.isChecked();
        boolean allowsPets = cbAllowPets.isChecked();

        String musicPref = getMusicPreference();
        String conversationPref = getConversationPreference();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timeMillis = selectedDateTime.getTimeInMillis();

        showLoading(true);

        // Geocode addresses
        geocodingService.getGeoPointsFromAddresses(from, to, new GeocodingService.BatchGeocodingCallback() {
            @Override
            public void onSuccess(GeoPoint startLocation, GeoPoint endLocation) {
                saveOfferToFirestore(userId, from, to, startLocation, endLocation,
                        timeMillis, seats, price, allowsSmoking, allowsPets, musicPref, conversationPref);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(OfferCreateActivity.this, "Geocoding error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getMusicPreference() {
        int selectedId = rgMusic.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMusicYes) return "yes";
        if (selectedId == R.id.rbMusicNo) return "no";
        return "no-preference";
    }

    private String getConversationPreference() {
        int selectedId = rgConversation.getCheckedRadioButtonId();
        if (selectedId == R.id.rbConversationChatty) return "chatty";
        if (selectedId == R.id.rbConversationQuiet) return "quiet";
        return "no-preference";
    }

    private void saveOfferToFirestore(String userId, String origin, String dest,
                                      GeoPoint startLocation, GeoPoint endLocation,
                                      long timeMillis, int seats, double price,
                                      boolean allowsSmoking, boolean allowsPets,
                                      String musicPref, String conversationPref) {

        String rideId = db.collection("offers").document().getId();

        Map<String, Object> offerData = new HashMap<>();
        offerData.put("id", rideId);
        offerData.put("type", "offer");
        offerData.put("ownerUid", userId);
        offerData.put("from", origin);
        offerData.put("to", dest);
        offerData.put("origin", origin);
        offerData.put("destination", dest);
        offerData.put("timeMillis", timeMillis);
        offerData.put("seats", seats);
        offerData.put("dateTime", new Timestamp(new Date(timeMillis)));
        offerData.put("createdAt", FieldValue.serverTimestamp());
        offerData.put("status", "open");
        offerData.put("updatedAt", FieldValue.serverTimestamp());

        // NEW: Matching algorithm fields
        offerData.put("startLocation", startLocation);
        offerData.put("endLocation", endLocation);
        offerData.put("allowsSmoking", allowsSmoking);
        offerData.put("allowsPets", allowsPets);
        offerData.put("musicPreference", musicPref);
        offerData.put("conversationLevel", conversationPref);
        offerData.put("driverRating", 5.0);
        offerData.put("pricePerSeat", price);

        db.collection("offers")
                .document(rideId)
                .set(offerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer created!", Toast.LENGTH_SHORT).show();

                    // Increment active post count
                    com.booknest.campusridenest.data.repo.ProfileRepository.getInstance()
                            .incrementActivePosts(userId)
                            .addOnFailureListener(e -> {
                                android.util.Log.e("OfferCreateActivity", "Failed to update profile stats", e);
                            });

                    showLoading(false);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSubmit.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geocodingService.shutdown();
    }
}