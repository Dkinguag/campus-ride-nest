package com.booknest.campusridenest.ui;

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

public class RequestCreateActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_request_create);


        EditText etOrigin = findViewById(R.id.etOriginR);
        EditText etDest   = findViewById(R.id.etDestinationR);
        EditText etSeats  = findViewById(R.id.etSeatsR);
        Button btn        = findViewById(R.id.btnCreateRequest);

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

        btn.setOnClickListener(v -> {
            String origin   = String.valueOf(etOrigin.getText()).trim();
            String dest     = String.valueOf(etDest.getText()).trim();
            String seatsStr = String.valueOf(etSeats.getText()).trim(); // remove if not using seats

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

            long when = System.currentTimeMillis();

            btn.setEnabled(false);

            repo.createRequestAsync(u.getUid(), origin, dest, when, seats, "open")
                    .addOnSuccessListener(id -> {
                        Toast.makeText(this, "Request created!", Toast.LENGTH_SHORT).show();
                        etOrigin.setText(""); etDest.setText(""); etSeats.setText(""); btn.setEnabled(true);

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
}
