package com.booknest.campusridenest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.repo.OfferRepository;
import com.booknest.campusridenest.ui.posts.PostsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OfferCreateActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_offer_create);

        EditText etOrigin = findViewById(R.id.etOrigin);
        EditText etDest   = findViewById(R.id.etDestination);
        EditText etSeats  = findViewById(R.id.etSeats);
        Button btn        = findViewById(R.id.btnCreateOffer);

        OfferRepository repo = new OfferRepository();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Must be signed in
        if (user == null) {
            btn.setEnabled(false);
            Toast.makeText(this, "Sign in first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Refresh and gate by email verification
        user.reload().addOnCompleteListener(task -> {
            FirebaseUser u = auth.getCurrentUser();
            boolean ok = u != null && u.isEmailVerified();
            btn.setEnabled(ok);
            if (!ok) {
                Toast.makeText(this, "Verify your email to create offers.", Toast.LENGTH_LONG).show();
            }
        });

        btn.setOnClickListener(v -> {
            String origin   = String.valueOf(etOrigin.getText()).trim();
            String dest     = String.valueOf(etDest.getText()).trim();
            String seatsStr = String.valueOf(etSeats.getText()).trim();

            if (TextUtils.isEmpty(origin)) { etOrigin.setError("Required"); return; }
            if (TextUtils.isEmpty(dest))   { etDest.setError("Required");   return; }
            if (TextUtils.isEmpty(seatsStr)) { etSeats.setError("Required"); return; }

            int seats;
            try { seats = Math.max(1, Integer.parseInt(seatsStr)); }
            catch (NumberFormatException nfe) { etSeats.setError("Number"); return; }

            FirebaseUser u = auth.getCurrentUser();
            if (u == null) { Toast.makeText(this, "Not signed in.", Toast.LENGTH_LONG).show(); return; }

            long when = System.currentTimeMillis();

            btn.setEnabled(false);
            repo.createOfferAsync(u.getUid(), origin, dest, when, seats, "open")
                    .addOnSuccessListener(id -> {
                        Toast.makeText(this, "Offer created!", Toast.LENGTH_SHORT).show();

                        etOrigin.setText("");
                        etDest.setText("");
                        etSeats.setText("");
                        btn.setEnabled(true);

                        // Go to the posts list (optionally to the "Mine" tab)
                        Intent i = new Intent(this, PostsActivity.class);
                        i.putExtra("tab", "mine"); // or "browse"
                        startActivity(i);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        btn.setEnabled(true);
                    });
        });
    }
}

