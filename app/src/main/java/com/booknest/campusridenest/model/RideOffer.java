package com.booknest.campusridenest.model;

import com.google.firebase.firestore.GeoPoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class RideOffer implements Serializable {

    @Nullable public String type;  // "offer"

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
    @Nullable public Object createdAt;
    @Nullable public Object status;
    @Nullable public Object updatedAt;

    // NEW: GeoPoint fields for algorithmic matching
    @Nullable public GeoPoint startLocation;
    @Nullable public GeoPoint endLocation;

    // NEW: Preference fields for matching
    public boolean allowsSmoking;
    public boolean allowsPets;
    @Nullable public String musicPreference;
    @Nullable public String conversationLevel;

    // NEW: Driver info for matching
    public double driverRating;
    public double pricePerSeat;

    // Default constructor (required by Firestore)
    public RideOffer() {
        this.type = "offer";
        this.allowsSmoking = false;
        this.allowsPets = false;
        this.musicPreference = "no-preference";
        this.conversationLevel = "no-preference";
        this.driverRating = 5.0;
        this.pricePerSeat = 0.0;
    }

    // Updated constructor with all fields
    public RideOffer(@Nullable String id,
                     @Nullable String ownerUid,
                     @Nullable String from,
                     @Nullable String to,
                     long timeMillis,
                     int seats,
                     @Nullable Object dateTime,
                     @Nullable Object createdAt,
                     @Nullable Object status,
                     @Nullable Object updatedAt,
                     @Nullable GeoPoint startLocation,
                     @Nullable GeoPoint endLocation,
                     boolean allowsSmoking,
                     boolean allowsPets,
                     @Nullable String musicPreference,
                     @Nullable String conversationLevel,
                     double driverRating,
                     double pricePerSeat) {
        this.type = "offer";
        this.id = id;
        this.ownerUid = ownerUid;
        this.from = from;
        this.to = to;
        this.origin = from;
        this.destination = to;
        this.timeMillis = timeMillis;
        this.seats = seats;
        this.dateTime = dateTime;
        this.createdAt = createdAt;
        this.status = status;
        this.updatedAt = updatedAt;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.allowsSmoking = allowsSmoking;
        this.allowsPets = allowsPets;
        this.musicPreference = musicPreference != null ? musicPreference : "no-preference";
        this.conversationLevel = conversationLevel != null ? conversationLevel : "no-preference";
        this.driverRating = driverRating;
        this.pricePerSeat = pricePerSeat;
    }

    // Convenience constructor
    public RideOffer(@Nullable String id,
                     @Nullable String ownerUid,
                     @Nullable String from,
                     @Nullable String to,
                     long timeMillis,
                     int seats,
                     @Nullable Object dateTime,
                     @Nullable Object createdAt,
                     @Nullable Object status,
                     @Nullable Object updatedAt) {
        this(id, ownerUid, from, to, timeMillis, seats, dateTime, createdAt, status, updatedAt,
                null, null, false, false, "no-preference", "no-preference", 5.0, 0.0);
    }

    @NotNull
    public RideOffer copy(@NotNull String newId) {
        return new RideOffer(
                newId,
                this.ownerUid,
                this.from,
                this.to,
                this.timeMillis,
                this.seats,
                this.dateTime,
                this.createdAt,
                this.status,
                this.updatedAt,
                this.startLocation,
                this.endLocation,
                this.allowsSmoking,
                this.allowsPets,
                this.musicPreference,
                this.conversationLevel,
                this.driverRating,
                this.pricePerSeat
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

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public Object getStatus() { return status; }
    public void setStatus(Object status) { this.status = status; }

    public Object getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }

    public GeoPoint getStartLocation() { return startLocation; }
    public void setStartLocation(GeoPoint startLocation) { this.startLocation = startLocation; }

    public GeoPoint getEndLocation() { return endLocation; }
    public void setEndLocation(GeoPoint endLocation) { this.endLocation = endLocation; }

    public boolean getAllowsSmoking() { return allowsSmoking; }
    public void setAllowsSmoking(boolean allowsSmoking) { this.allowsSmoking = allowsSmoking; }

    public boolean getAllowsPets() { return allowsPets; }
    public void setAllowsPets(boolean allowsPets) { this.allowsPets = allowsPets; }

    public String getMusicPreference() { return musicPreference; }
    public void setMusicPreference(String musicPreference) { this.musicPreference = musicPreference; }

    public String getConversationLevel() { return conversationLevel; }
    public void setConversationLevel(String conversationLevel) { this.conversationLevel = conversationLevel; }

    public double getDriverRating() { return driverRating; }
    public void setDriverRating(double driverRating) { this.driverRating = driverRating; }

    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }
}