package com.booknest.campusridenest.data;

import com.booknest.campusridenest.model.RideOffer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class OfferRepository {

    private static final String OFFERS = "offers";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> create(RideOffer o) {
        if (o == null) throw new IllegalArgumentException("offer is null");
        if (o.id == null || o.id.isEmpty()) {
            o.id = db.collection(OFFERS).document().getId();
        }
        return db.collection(OFFERS).document(o.id).set(o);
    }

    public Task<Void> create(String ownerUid, String origin, String destination, int seats) {
        long now = System.currentTimeMillis();
        RideOffer o = new RideOffer(null, ownerUid, origin, destination, now, seats);
        return create(o);
    }
}

