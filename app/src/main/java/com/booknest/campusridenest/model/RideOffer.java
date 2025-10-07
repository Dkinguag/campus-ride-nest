package com.booknest.campusridenest.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class RideOffer implements Serializable {

    @Nullable public String id;

    @Nullable public String ownerUid;
    @Nullable public String origin;
    @Nullable public String destination;

    public long timeMillis;

    public int seats;

    @Nullable public Object from;

    @Nullable public Object dateTime;

    @Nullable public Object createdAt;

    @Nullable public Object status;

    @Nullable public Object updatedAt;


    public RideOffer() {}

    public RideOffer(@Nullable String id,
                     @Nullable String ownerUid,
                     @Nullable String origin,
                     @Nullable String destination,
                     long timeMillis,
                     int seats,
                     @Nullable Object from,
                     @Nullable Object dateTime,
                     @Nullable Object createdAt,
                     @Nullable Object status) {
        this.id = id;
        this.ownerUid = ownerUid;
        this.origin = origin;
        this.destination = destination;
        this.timeMillis = timeMillis;
        this.seats = seats;
        this.from = from;
        this.dateTime = dateTime;
        this.createdAt = createdAt;
        this.status = status;
    }

    public RideOffer(@Nullable String id,
                     @Nullable String ownerUid,
                     @Nullable String origin,
                     @Nullable String destination,
                     long timeMillis,
                     int seats,
                     @Nullable Object from,
                     @Nullable Object dateTime,
                     @Nullable Object createdAt,
                     @Nullable Object status,
                     @Nullable Object updatedAt) {
        this(id, ownerUid, origin, destination, timeMillis, seats, from, dateTime, createdAt, status);
        this.updatedAt = updatedAt;
    }

    @NotNull
    public RideOffer copy(@NotNull String newId) {
        return new RideOffer(
                newId,
                this.ownerUid,
                this.origin,
                this.destination,
                this.timeMillis,
                this.seats,
                this.from,
                this.dateTime,
                this.createdAt,
                this.status,
                this.updatedAt
        );
    }
}
