package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.booknest.campusridenest.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Create handler with proper cleanup
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                // Only navigate if activity is still active
                if (!isFinishing() && !isDestroyed()) {
                    navigateToNextScreen();
                }
            }
        };

        // Post with delay
        handler.postDelayed(runnable, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CRITICAL: Remove callbacks to prevent memory leaks and crashes
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clean up if user leaves before delay completes
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}