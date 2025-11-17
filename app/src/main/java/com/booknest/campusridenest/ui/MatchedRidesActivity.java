package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchedRidesActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_rides);

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
                    adapter = new MatchedRidesAdapter(matches);
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