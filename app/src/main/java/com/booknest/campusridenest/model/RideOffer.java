package com.booknest.campusridenest.model;

public class RideOffer {
    public String id;
    public String ownerUid;
    public String origin;
    public String destination;
    public long   timeMillis;
    public int    seats;

    public RideOffer() { }

    public RideOffer(String id,
                     String ownerUid,
                     String origin,
                     String destination,
                     long timeMillis,
                     int seats) {
        this.id = id;
        this.ownerUid = ownerUid;
        this.origin = origin;
        this.destination = destination;
        this.timeMillis = timeMillis;
        this.seats = seats;
    }
}
