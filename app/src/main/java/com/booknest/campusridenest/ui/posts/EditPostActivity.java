package com.booknest.campusridenest.ui.posts;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.viewmodel.EditPostViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * EditPostActivity - Allows users to edit their existing posts
 * Sprint 3 US-05: Edit a post
 */
public class EditPostActivity extends AppCompatActivity {

    // Intent extras for passing post data
    public static final String EXTRA_POST_TYPE = "post_type";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_TO = "to";
    public static final String EXTRA_DATE_TIME = "date_time";
    public static final String EXTRA_SEATS = "seats";
    public static final String EXTRA_PRICE = "price";

    // UI Components
    private TextInputLayout tilFrom, tilTo, tilSeats, tilPrice;
    private EditText etFrom, etTo, etSeats, etPrice;
    private TextView tvSelectedDateTime;
    private Button btnSelectDate, btnSelectTime, btnSaveChanges;
    private ProgressBar progressBar;

    // ViewModel
    private EditPostViewModel viewModel;

    // Post data
    private String postType;
    private String postId;
    private long selectedDateTimeMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(EditPostViewModel.class);

        // Set up toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Post");
        }

        // Initialize views
        initializeViews();

        // Load post data from intent
        loadPostData();

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        tilFrom = findViewById(R.id.til_from);
        tilTo = findViewById(R.id.til_to);
        tilSeats = findViewById(R.id.til_seats);
        tilPrice = findViewById(R.id.til_price);

        etFrom = findViewById(R.id.et_from);
        etTo = findViewById(R.id.et_to);
        etSeats = findViewById(R.id.et_seats);
        etPrice = findViewById(R.id.et_price);

        tvSelectedDateTime = findViewById(R.id.tv_selected_datetime);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSelectTime = findViewById(R.id.btn_select_time);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadPostData() {
        // Get post data from intent
        postType = getIntent().getStringExtra(EXTRA_POST_TYPE);
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        String from = getIntent().getStringExtra(EXTRA_FROM);
        String to = getIntent().getStringExtra(EXTRA_TO);
        long dateTimeMillis = getIntent().getLongExtra(EXTRA_DATE_TIME, 0);
        int seats = getIntent().getIntExtra(EXTRA_SEATS, 1);

        // Pre-populate form fields
        etFrom.setText(from);
        etTo.setText(to);
        etSeats.setText(String.valueOf(seats));

        selectedDateTimeMillis = dateTimeMillis;
        updateDateTimeDisplay();

        // Handle price field visibility (offers only)
        if ("offer".equals(postType)) {
            tilPrice.setVisibility(View.VISIBLE);
            int price = getIntent().getIntExtra(EXTRA_PRICE, 0);
            etPrice.setText(String.valueOf(price));
        } else {
            tilPrice.setVisibility(View.GONE);
        }

        // Set post data in ViewModel
        viewModel.setPostData(postType, postId, from, to, dateTimeMillis, seats,
                "offer".equals(postType) ? getIntent().getIntExtra(EXTRA_PRICE, 0) : 0);
    }

    private void setupObservers() {
        // Observe validation errors
        viewModel.getFromError().observe(this, error -> tilFrom.setError(error));
        viewModel.getToError().observe(this, error -> tilTo.setError(error));
        viewModel.getSeatsError().observe(this, error -> tilSeats.setError(error));
        viewModel.getPriceError().observe(this, error -> tilPrice.setError(error));
        viewModel.getDateTimeError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe save state
        viewModel.getSaveState().observe(this, state -> {
            if (state == null) return;

            switch (state) {
                case SAVING:
                    showLoading(true);
                    btnSaveChanges.setEnabled(false);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;

                case ERROR:
                    showLoading(false);
                    btnSaveChanges.setEnabled(true);
                    String errorMsg = viewModel.getErrorMessage().getValue();
                    Toast.makeText(this,
                            errorMsg != null ? errorMsg : "Failed to update post",
                            Toast.LENGTH_SHORT).show();
                    break;

                case IDLE:
                    showLoading(false);
                    btnSaveChanges.setEnabled(true);
                    break;
            }
        });
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDateTimeMillis > 0) {
            calendar.setTimeInMillis(selectedDateTimeMillis);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Preserve time if already set
                    if (selectedDateTimeMillis > 0) {
                        Calendar existing = Calendar.getInstance();
                        existing.setTimeInMillis(selectedDateTimeMillis);
                        selected.set(Calendar.HOUR_OF_DAY, existing.get(Calendar.HOUR_OF_DAY));
                        selected.set(Calendar.MINUTE, existing.get(Calendar.MINUTE));
                    }

                    selectedDateTimeMillis = selected.getTimeInMillis();
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Don't allow past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDateTimeMillis > 0) {
            calendar.setTimeInMillis(selectedDateTimeMillis);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    if (selectedDateTimeMillis > 0) {
                        selected.setTimeInMillis(selectedDateTimeMillis);
                    }
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);

                    selectedDateTimeMillis = selected.getTimeInMillis();
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );

        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        if (selectedDateTimeMillis > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            tvSelectedDateTime.setText(dateFormat.format(new Date(selectedDateTimeMillis)));
            tvSelectedDateTime.setVisibility(View.VISIBLE);
        } else {
            tvSelectedDateTime.setVisibility(View.GONE);
        }
    }

    private void saveChanges() {
        // Clear previous errors
        tilFrom.setError(null);
        tilTo.setError(null);
        tilSeats.setError(null);
        tilPrice.setError(null);

        // Get form values
        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String seatsStr = etSeats.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // Parse numeric values
        int seats = 0;
        int price = 0;

        try {
            if (!seatsStr.isEmpty()) {
                seats = Integer.parseInt(seatsStr);
            }
        } catch (NumberFormatException e) {
            tilSeats.setError("Invalid number");
            return;
        }

        if ("offer".equals(postType)) {
            try {
                if (!priceStr.isEmpty()) {
                    price = Integer.parseInt(priceStr);
                }
            } catch (NumberFormatException e) {
                tilPrice.setError("Invalid number");
                return;
            }
        }

        // Update post via ViewModel
        viewModel.updatePost(from, to, selectedDateTimeMillis, seats, price);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
