package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.AuthRepository;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        EditText email = findViewById(R.id.etEmailL);
        EditText pass  = findViewById(R.id.etPassL);
        TextView status= findViewById(R.id.tvStatus);
        Button btn     = findViewById(R.id.btnLogin);
        AuthRepository repo = new AuthRepository();

        btn.setOnClickListener(v ->
                repo.signIn(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(r -> {
                            boolean ok = repo.isVerified();
                            status.setText(ok ? "Verified — you can post." :
                                    "Not verified — check your email.");

                            Intent i = new Intent(LoginActivity.this, com.booknest.campusridenest.ui.posts.PostsActivity.class);
                            // default to the "browse" tab so I see EVERYTHING
                            i.putExtra("tab", "browse");
                            startActivity(i);
                            finish();
                        })
                        .addOnFailureListener(e -> status.setText(e.getMessage()))
        );
    }
}

