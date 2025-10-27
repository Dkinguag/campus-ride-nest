package com.booknest.campusridenest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.booknest.campusridenest.util.ValidationRules;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * EditPostViewModel - Manages edit post form state and validation
 * Sprint 3 US-05: Edit a post
 */
public class EditPostViewModel extends ViewModel {

    public enum SaveState {
        IDLE, SAVING, SUCCESS, ERROR
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Post data
    private String postType;
    private String postId;

    // Form validation errors
    private final MutableLiveData<String> fromError = new MutableLiveData<>();
    private final MutableLiveData<String> toError = new MutableLiveData<>();
    private final MutableLiveData<String> seatsError = new MutableLiveData<>();
    private final MutableLiveData<String> priceError = new MutableLiveData<>();
    private final MutableLiveData<String> dateTimeError = new MutableLiveData<>();

    // Save state
    private final MutableLiveData<SaveState> saveState = new MutableLiveData<>(SaveState.IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Getters for LiveData
    public LiveData<String> getFromError() {
        return fromError;
    }

    public LiveData<String> getToError() {
        return toError;
    }

    public LiveData<String> getSeatsError() {
        return seatsError;
    }

    public LiveData<String> getPriceError() {
        return priceError;
    }

    public LiveData<String> getDateTimeError() {
        return dateTimeError;
    }

    public LiveData<SaveState> getSaveState() {
        return saveState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setPostData(String postType, String postId, String from, String to,
                            long dateTimeMillis, int seats, int price) {
        this.postType = postType;
        this.postId = postId;
    }

    public void updatePost(String from, String to, long dateTimeMillis, int seats, int price) {
        // Clear previous errors
        fromError.setValue(null);
        toError.setValue(null);
        seatsError.setValue(null);
        priceError.setValue(null);
        dateTimeError.setValue(null);

        // Validate all fields using ValidationRules (Sprint 2 Week 7)
        boolean isValid = true;

        // Validate origin
        String originError = ValidationRules.validateOrigin(from);
        if (originError != null) {
            fromError.setValue(originError);
            isValid = false;
        }

        // Validate destination
        String destinationError = ValidationRules.validateDestination(to);
        if (destinationError != null) {
            toError.setValue(destinationError);
            isValid = false;
        }

        // Validate seats
        String seatsValidationError = ValidationRules.validateSeats(seats);
        if (seatsValidationError != null) {
            seatsError.setValue(seatsValidationError);
            isValid = false;
        }

        // Validate price (offers only)
        if ("offer".equals(postType)) {
            String priceValidationError = ValidationRules.validatePrice(price);
            if (priceValidationError != null) {
                priceError.setValue(priceValidationError);
                isValid = false;
            }
        }

        // Validate date/time (must be in future)
        String dateTimeValidationError = ValidationRules.validateFutureDateTime(dateTimeMillis);
        if (dateTimeValidationError != null) {
            dateTimeError.setValue(dateTimeValidationError);
            isValid = false;
        }

        // If validation fails, stop here
        if (!isValid) {
            return;
        }

        // Proceed with Firestore update
        performFirestoreUpdate(from, to, dateTimeMillis, seats, price);
    }
    private void performFirestoreUpdate(String from, String to, long dateTimeMillis,
                                        int seats, int price) {
        saveState.setValue(SaveState.SAVING);

        // Build updates map
        Map<String, Object> updates = new HashMap<>();
        updates.put("from", from);
        updates.put("to", to);
        updates.put("dateTime", new Timestamp(dateTimeMillis / 1000, 0));
        updates.put("seats", seats);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        // Add price for offers
        if ("offer".equals(postType)) {
            updates.put("price", price);
        }

        // Determine collection
        String collection = "offer".equals(postType) ? "offers" : "requests";

        // Update Firestore
        db.collection(collection)
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    saveState.setValue(SaveState.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    saveState.setValue(SaveState.ERROR);
                    errorMessage.setValue("Failed to update post: " + e.getMessage());
                });
    }
}