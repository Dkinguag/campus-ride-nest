package com.booknest.campusridenest.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public Task<AuthResult> signUp(String email, String pass) {
        return auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    FirebaseUser u = auth.getCurrentUser();
                    if (u != null) u.sendEmailVerification();
                });
    }

    public Task<AuthResult> signIn(String email, String pass) {
        return auth.signInWithEmailAndPassword(email, pass);
    }

    public boolean isVerified() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null && u.isEmailVerified();
    }

    public String uid() {
        FirebaseUser u = auth.getCurrentUser();
        return (u == null) ? null : u.getUid();
    }

}
