package com.booknest.campusridenest.data;

import com.booknest.campusridenest.model.RideRequest;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestRepository {
    private static final String REQUESTS = "requests";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> create(RideRequest r) {
        if (r == null) throw new IllegalArgumentException("request is null");
        if (r.id == null || r.id.isEmpty()) {
            r.id = db.collection(REQUESTS).document().getId();
        }
        return db.collection(REQUESTS).document(r.id).set(r);
    }
}
