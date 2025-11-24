package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.ui.adapters.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private Button btnNext;
    private TextView tvSkip;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);

        // Set up ViewPager adapter
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Set up page indicators
        setupPageIndicators();
        setCurrentIndicator(0);

        // ViewPager page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                // Change button text on last slide
                if (position == 2) { // Last slide (0-indexed)
                    btnNext.setText("Get Started");
                    tvSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("Next");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        // Next button click
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() < 2) {
                    // Move to next slide
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                } else {
                    // Last slide - finish onboarding
                    finishOnboarding();
                }
            }
        });

        // Skip button click
        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });
    }

    private void setupPageIndicators() {
        // Create 3 indicator dots
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.indicator_inactive);
            dotsLayout.addView(dot);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = dotsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View dot = dotsLayout.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.indicator_active);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_inactive);
            }
        }
    }

    private void finishOnboarding() {
        // Mark onboarding as complete
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();

        // Navigate to login screen
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}