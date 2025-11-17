package com.booknest.campusridenest.model;

import com.google.firebase.firestore.GeoPoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class RideRequest implements Serializable {

    @Nullable public String type;  // "request"

    // Existing fields
    @Nullable public String id;
    @Nullable public String ownerUid;

    @Nullable public String from;
    @Nullable public String to;

    @Nullable public String origin;
    @Nullable public String destination;

    public long timeMillis;
    public int seats;

    @Nullable public Object dateTime;
    @Nullable public Object status;
    @Nullable public Object createdAt;
    @Nullable public Object updatedAt;

    // NEW: GeoPoint fields
    @Nullable public GeoPoint pickupLocation;
    @Nullable public GeoPoint dropoffLocation;

    // NEW: Rider preference fields
    public boolean needsNonSmoking;
    public boolean needsNoPets;
    @Nullable public String musicPreference;
    @Nullable public String conversationLevel;

    // NEW: Budget constraint
    public double maxBudget;

    // Default constructor (required by Firestore)
    public RideRequest() {
        this.type = "request";
        this.needsNonSmoking = false;
        this.needsNoPets = false;
        this.musicPreference = "no-preference";
        this.conversationLevel = "no-preference";
        this.maxBudget = 0.0;
    }

    // Updated constructor with all fields
    public RideRequest(@Nullable String id,
                       @Nullable String ownerUid,
                       @Nullable String from,
                       @Nullable String to,
                       long timeMillis,
                       int seats,
                       @Nullable Object dateTime,
                       @Nullable Object status,
                       @Nullable Object createdAt,
                       @Nullable Object updatedAt,
                       @Nullable GeoPoint pickupLocation,
                       @Nullable GeoPoint dropoffLocation,
                       boolean needsNonSmoking,
                       boolean needsNoPets,
                       @Nullable String musicPreference,
                       @Nullable String conversationLevel,
                       double maxBudget) {
        this.type = "request";
        this.id = id;
        this.ownerUid = ownerUid;
        this.from = from;
        this.to = to;
        this.origin = from;
        this.destination = to;
        this.timeMillis = timeMillis;
        this.seats = seats;
        this.dateTime = dateTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.needsNonSmoking = needsNonSmoking;
        this.needsNoPets = needsNoPets;
        this.musicPreference = musicPreference != null ? musicPreference : "no-preference";
        this.conversationLevel = conversationLevel != null ? conversationLevel : "no-preference";
        this.maxBudget = maxBudget;
    }

    // Convenience constructor
    public RideRequest(@Nullable String id,
                       @Nullable String ownerUid,
                       @Nullable String from,
                       @Nullable String to,
                       long timeMillis,
                       int seats,
                       @Nullable Object dateTime,
                       @Nullable Object status,
                       @Nullable Object createdAt,
                       @Nullable Object updatedAt) {
        this(id, ownerUid, from, to, timeMillis, seats, dateTime, status, createdAt, updatedAt,
                null, null, false, false, "no-preference", "no-preference", 0.0);
    }

    @NotNull
    public RideRequest copy(@NotNull String newId) {
        return new RideRequest(
                newId,
                this.ownerUid,
                this.from,
                this.to,
                this.timeMillis,
                this.seats,
                this.dateTime,
                this.status,
                this.createdAt,
                this.updatedAt,
                this.pickupLocation,
                this.dropoffLocation,
                this.needsNonSmoking,
                this.needsNoPets,
                this.musicPreference,
                this.conversationLevel,
                this.maxBudget
        );
    }

    // ============ FIRESTORE GETTERS/SETTERS ============

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }

    public String getFrom() { return from; }
    public void setFrom(String from) {
        this.from = from;
        this.origin = from;
    }

    public String getTo() { return to; }
    public void setTo(String to) {
        this.to = to;
        this.destination = to;
    }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public long getTimeMillis() { return timeMillis; }
    public void setTimeMillis(long timeMillis) { this.timeMillis = timeMillis; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public Object getDateTime() { return dateTime; }
    public void setDateTime(Object dateTime) { this.dateTime = dateTime; }

    public Object getStatus() { return status; }
    public void setStatus(Object status) { this.status = status; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public Object getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }

    public GeoPoint getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(GeoPoint pickupLocation) { this.pickupLocation = pickupLocation; }

    public GeoPoint getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(GeoPoint dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public boolean getNeedsNonSmoking() { return needsNonSmoking; }
    public void setNeedsNonSmoking(boolean needsNonSmoking) { this.needsNonSmoking = needsNonSmoking; }

    public boolean getNeedsNoPets() { return needsNoPets; }
    public void setNeedsNoPets(boolean needsNoPets) { this.needsNoPets = needsNoPets; }

    public String getMusicPreference() { return musicPreference; }
    public void setMusicPreference(String musicPreference) { this.musicPreference = musicPreference; }

    public String getConversationLevel() { return conversationLevel; }
    public void setConversationLevel(String conversationLevel) { this.conversationLevel = conversationLevel; }

    public double getMaxBudget() { return maxBudget; }
    public void setMaxBudget(double maxBudget) { this.maxBudget = maxBudget; }
}