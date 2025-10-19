package com.booknest.campusridenest.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class RideRequest implements Serializable {
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

    public RideRequest() {}

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
                this.updatedAt
        );
    }
}