package com.booknest.campusridenest.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.repo.RequestRepository;
import com.booknest.campusridenest.ui.posts.PostsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class RequestCreateActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button dateButton;
    private Button timeButton;

    // Store selected date and time
    private int selectedYear = -1;
    private int selectedMonth = -1;
    private int selectedDay = -1;
    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_request_create);

        EditText etOrigin = findViewById(R.id.etOriginR);
        EditText etDest   = findViewById(R.id.etDestinationR);
        EditText etSeats  = findViewById(R.id.etSeatsR);
        Button btn        = findViewById(R.id.btnCreateRequest);

        // Initialize date and time buttons
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);

        RequestRepository repo = new RequestRepository();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            btn.setEnabled(false);
            Toast.makeText(this, "Sign in first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Same email verification gate as offers
        user.reload().addOnCompleteListener(task -> {
            FirebaseUser u = auth.getCurrentUser();
            boolean ok = u != null && u.isEmailVerified();
            btn.setEnabled(ok);
            if (!ok) {
                Toast.makeText(this, "Verify your email to create requests.", Toast.LENGTH_LONG).show();
            }
        });

        // Date button click listener
        dateButton.setOnClickListener(v -> showDatePicker());

        // Time button click listener
        timeButton.setOnClickListener(v -> showTimePicker());

        btn.setOnClickListener(v -> {
            String origin   = String.valueOf(etOrigin.getText()).trim();
            String dest     = String.valueOf(etDest.getText()).trim();
            String seatsStr = String.valueOf(etSeats.getText()).trim();

            if (TextUtils.isEmpty(origin)) { etOrigin.setError("Required"); return; }
            if (TextUtils.isEmpty(dest))   { etDest.setError("Required");   return; }

            int seats = 1;
            try {
                if (!TextUtils.isEmpty(seatsStr)) seats = Math.max(1, Integer.parseInt(seatsStr));
            } catch (NumberFormatException nfe) {
                etSeats.setError("Number"); return;
            }

            FirebaseUser u = auth.getCurrentUser();
            if (u == null) { Toast.makeText(this, "Not signed in.", Toast.LENGTH_LONG).show(); return; }

            // Check if date and time are selected
            if (selectedYear == -1 || selectedHour == -1) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create timestamp from selected date and time
            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
            long when = calendar.getTimeInMillis();

            btn.setEnabled(false);

            repo.createRequestAsync(u.getUid(), origin, dest, when, seats, "open")
                    .addOnSuccessListener(id -> {
                        Toast.makeText(this, "Request created!", Toast.LENGTH_SHORT).show();

                        etOrigin.setText("");
                        etDest.setText("");
                        etSeats.setText("");
                        btn.setEnabled(true);

                        // Reset date and time selections
                        selectedYear = -1;
                        selectedMonth = -1;
                        selectedDay = -1;
                        selectedHour = -1;
                        selectedMinute = -1;
                        dateButton.setText("Select Date");
                        timeButton.setText("Select Time");

                        Intent i = new Intent(this, PostsActivity.class);
                        i.putExtra("tab", "mine");
                        startActivity(i);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        btn.setEnabled(true);
                    });
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Save selected date
                    this.selectedYear = selectedYear;
                    this.selectedMonth = selectedMonth;
                    this.selectedDay = selectedDay;

                    // Update button text
                    String dateText = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    dateButton.setText(dateText);
                },
                year, month, day
        );

        // Don't allow selecting past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Save selected time
                    this.selectedHour = selectedHour;
                    this.selectedMinute = selectedMinute;

                    // Update button text
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int displayHour = selectedHour % 12;
                    if (displayHour == 0) displayHour = 12;
                    String timeText = String.format("%d:%02d %s", displayHour, selectedMinute, amPm);
                    timeButton.setText(timeText);
                },
                hour, minute, false // false = 12-hour format
        );

        timePickerDialog.show();
    }
}