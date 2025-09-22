package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.RequestRepository;
import com.booknest.campusridenest.model.RideRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RequestCreateActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_request_create);

        EditText o = findViewById(R.id.etOriginR);
        EditText d = findViewById(R.id.etDestinationR);
        EditText s = findViewById(R.id.etSeatsR);
        Button btn = findViewById(R.id.btnCreateRequest);

        RequestRepository repo = new RequestRepository();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            btn.setEnabled(false);
            Toast.makeText(this, "Sign in first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Require email verification just like Offers
        user.reload().addOnCompleteListener(t -> {
            FirebaseUser u = auth.getCurrentUser();
            boolean ok = (u != null && u.isEmailVerified());
            btn.setEnabled(ok);
            if (!ok) Toast.makeText(this, "Verify your email to create requests.", Toast.LENGTH_LONG).show();
        });

        btn.setOnClickListener(v -> {
            String origin = o.getText().toString().trim();
            String dest   = d.getText().toString().trim();
            String seatStr= s.getText().toString().trim();

            if (TextUtils.isEmpty(origin)) { o.setError("Required"); return; }
            if (TextUtils.isEmpty(dest))   { d.setError("Required"); return; }
            if (TextUtils.isEmpty(seatStr)){ s.setError("Required"); return; }

            int seats;
            try { seats = Math.max(1, Integer.parseInt(seatStr)); }
            catch (NumberFormatException nfe) { s.setError("Number"); return; }

            FirebaseUser u = auth.getCurrentUser();
            if (u == null) { Toast.makeText(this, "Not signed in.", Toast.LENGTH_LONG).show(); return; }

            RideRequest req = new RideRequest(
                    null, u.getUid(), origin, dest, System.currentTimeMillis(), seats
            );

            btn.setEnabled(false);
            repo.create(req)
                    .addOnSuccessListener(x -> {
                        Toast.makeText(this, "Request created!", Toast.LENGTH_SHORT).show();
                        o.setText(""); d.setText(""); s.setText("");
                        btn.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        btn.setEnabled(true);
                    });
        });
    }
}
