package com.booknest.campusridenest.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;


public class RideRequest implements Serializable {
    @Nullable public String id;

    @Nullable public String ownerUid;
    @Nullable public String origin;
    @Nullable public String destination;

    public long timeMillis;
    public int seats;

    @Nullable public Object from;
    @Nullable public Object dateTime;

    @Nullable public Object status;

    @Nullable public Object createdAt;
    @Nullable public Object updatedAt;

    public RideRequest() {}

    public RideRequest(@Nullable String id,
                       @Nullable String ownerUid,
                       @Nullable String origin,
                       @Nullable String destination,
                       long timeMillis,
                       int seats,
                       @Nullable Object from,
                       @Nullable Object dateTime,
                       @Nullable Object status,
                       @Nullable Object createdAt,
                       @Nullable Object updatedAt) {
        this.id = id;
        this.ownerUid = ownerUid;
        this.origin = origin;
        this.destination = destination;
        this.timeMillis = timeMillis;
        this.seats = seats;
        this.from = from;
        this.dateTime = dateTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NotNull
    public RideRequest copy(@NotNull String newId) {
        return new RideRequest(
                newId,
                this.ownerUid,
                this.origin,
                this.destination,
                this.timeMillis,
                this.seats,
                this.from,
                this.dateTime,
                this.status,
                this.createdAt,
                this.updatedAt
        );
    }
}
