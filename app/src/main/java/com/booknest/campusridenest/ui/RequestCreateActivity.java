package com.booknest.campusridenest.ui;

import android.content.Intent;
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

public class RequestCreateActivity extends AppCompatActivity {

    private EditText etFrom, etTo, etSeats, etMaxBudget;
    private Button btnPickDate, btnPickTime, btnSubmit;
    private ProgressBar progressBar;

    // NEW: Preference UI elements
    private CheckBox cbNeedNonSmoking, cbNeedNoPets;
    private RadioGroup rgMusic, rgConversation;

    private FirebaseFirestore db;
    private GeocodingService geocodingService;

    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_create);

        db = FirebaseFirestore.getInstance();
        geocodingService = new GeocodingService(this);
        selectedDateTime = Calendar.getInstance();

        // Initialize views
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etSeats = findViewById(R.id.etSeats);
        etMaxBudget = findViewById(R.id.etMaxBudget);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // NEW: Initialize preference views
        cbNeedNonSmoking = findViewById(R.id.cbNeedNonSmoking);
        cbNeedNoPets = findViewById(R.id.cbNeedNoPets);
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
        String budgetStr = etMaxBudget.getText().toString().trim();

        // Validation
        if (from.isEmpty() || to.isEmpty() || seatsStr.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats = Integer.parseInt(seatsStr);
        double maxBudget = Double.parseDouble(budgetStr);

        // Get preference values
        boolean needsNonSmoking = cbNeedNonSmoking.isChecked();
        boolean needsNoPets = cbNeedNoPets.isChecked();

        String musicPref = getMusicPreference();
        String conversationPref = getConversationPreference();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timeMillis = selectedDateTime.getTimeInMillis();

        showLoading(true);

        // Geocode addresses
        geocodingService.getGeoPointsFromAddresses(from, to, new GeocodingService.BatchGeocodingCallback() {
            @Override
            public void onSuccess(GeoPoint pickupLocation, GeoPoint dropoffLocation) {
                saveRequestToFirestore(userId, from, to, pickupLocation, dropoffLocation,
                        timeMillis, seats, maxBudget, needsNonSmoking, needsNoPets, musicPref, conversationPref);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(RequestCreateActivity.this, "Geocoding error: " + error, Toast.LENGTH_LONG).show();
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

    private void saveRequestToFirestore(String userId, String origin, String dest,
                                        GeoPoint pickupLocation, GeoPoint dropoffLocation,
                                        long timeMillis, int seats, double maxBudget,
                                        boolean needsNonSmoking, boolean needsNoPets,
                                        String musicPref, String conversationPref) {

        String requestId = db.collection("requests").document().getId();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("id", requestId);
        requestData.put("type", "request");
        requestData.put("ownerUid", userId);
        requestData.put("from", origin);
        requestData.put("to", dest);
        requestData.put("origin", origin);
        requestData.put("destination", dest);
        requestData.put("timeMillis", timeMillis);
        requestData.put("seats", seats);
        requestData.put("dateTime", new Timestamp(new Date(timeMillis)));
        requestData.put("status", "open");
        requestData.put("createdAt", FieldValue.serverTimestamp());
        requestData.put("updatedAt", FieldValue.serverTimestamp());

        // NEW: Matching algorithm fields
        requestData.put("pickupLocation", pickupLocation);
        requestData.put("dropoffLocation", dropoffLocation);
        requestData.put("needsNonSmoking", needsNonSmoking);
        requestData.put("needsNoPets", needsNoPets);
        requestData.put("musicPreference", musicPref);
        requestData.put("conversationLevel", conversationPref);
        requestData.put("maxBudget", maxBudget);

        db.collection("requests")
                .document(requestId)
                .set(requestData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request created!", Toast.LENGTH_SHORT).show();

                    // Increment active post count
                    com.booknest.campusridenest.data.repo.ProfileRepository.getInstance()
                            .incrementActivePosts(userId)
                            .addOnFailureListener(e -> {
                                android.util.Log.e("RequestCreateActivity", "Failed to update profile stats", e);
                            });

                    showLoading(false);

                    // Launch matching activity
                    Intent intent = new Intent(this, MatchedRidesActivity.class);
                    intent.putExtra("request_id", requestId);
                    intent.putExtra("from", origin);
                    intent.putExtra("to", dest);
                    intent.putExtra("timeMillis", timeMillis);
                    intent.putExtra("seats", seats);
                    intent.putExtra("maxBudget", maxBudget);
                    intent.putExtra("pickupLat", pickupLocation.getLatitude());
                    intent.putExtra("pickupLon", pickupLocation.getLongitude());
                    intent.putExtra("dropoffLat", dropoffLocation.getLatitude());
                    intent.putExtra("dropoffLon", dropoffLocation.getLongitude());
                    intent.putExtra("needsNonSmoking", needsNonSmoking);
                    intent.putExtra("needsNoPets", needsNoPets);
                    intent.putExtra("musicPreference", musicPref);
                    intent.putExtra("conversationLevel", conversationPref);
                    intent.putExtra("ownerUid", userId);

                    startActivity(intent);
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