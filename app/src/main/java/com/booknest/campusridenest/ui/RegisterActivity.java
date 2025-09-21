package com.booknest.campusridenest.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.booknest.campusridenest.R;
import com.booknest.campusridenest.data.AuthRepository;

public class RegisterActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);

        EditText email = findViewById(R.id.etEmailR);
        EditText pass  = findViewById(R.id.etPassR);
        Button btn     = findViewById(R.id.btnRegister);
        AuthRepository repo = new AuthRepository();

        btn.setOnClickListener(v ->
                repo.signUp(email.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(x ->
                                Toast.makeText(this,"Check your email to verify.", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show())
        );
    }
}

