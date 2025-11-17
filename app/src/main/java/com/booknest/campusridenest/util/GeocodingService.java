package com.booknest.campusridenest.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeocodingService {
    private static final String TAG = "GeocodingService";
    private final Context context;
    private final Geocoder geocoder;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    // Fallback coordinates for common campus locations
    private static final Map<String, GeoPoint> FALLBACK_COORDS = new HashMap<>();
    static {
        // Penn State locations
        FALLBACK_COORDS.put("penn state", new GeoPoint(40.7982, -77.8599));
        FALLBACK_COORDS.put("penn state university", new GeoPoint(40.7982, -77.8599));
        FALLBACK_COORDS.put("hub", new GeoPoint(40.7967, -77.8617));
        FALLBACK_COORDS.put("pattee library", new GeoPoint(40.7994, -77.8611));
        FALLBACK_COORDS.put("beaver stadium", new GeoPoint(40.8122, -77.8563));

        // Common PA cities
        FALLBACK_COORDS.put("harrisburg", new GeoPoint(40.2737, -76.8844));
        FALLBACK_COORDS.put("harrisburg pa", new GeoPoint(40.2737, -76.8844));
        FALLBACK_COORDS.put("philadelphia", new GeoPoint(39.9526, -75.1652));
        FALLBACK_COORDS.put("philadelphia pa", new GeoPoint(39.9526, -75.1652));
        FALLBACK_COORDS.put("pittsburgh", new GeoPoint(40.4406, -79.9959));
        FALLBACK_COORDS.put("pittsburgh pa", new GeoPoint(40.4406, -79.9959));

        // Campus buildings
        FALLBACK_COORDS.put("main building", new GeoPoint(40.7985, -77.8600));
        FALLBACK_COORDS.put("library", new GeoPoint(40.7994, -77.8611));
        FALLBACK_COORDS.put("gym", new GeoPoint(40.8020, -77.8570));
        FALLBACK_COORDS.put("cafe", new GeoPoint(40.7970, -77.8620));
        FALLBACK_COORDS.put("ormsby hall", new GeoPoint(40.7975, -77.8590));
        FALLBACK_COORDS.put("sage hall", new GeoPoint(40.7980, -77.8595));
        FALLBACK_COORDS.put("campus facility", new GeoPoint(40.7990, -77.8610));
        FALLBACK_COORDS.put("scott hall", new GeoPoint(40.7965, -77.8585));
        FALLBACK_COORDS.put("mall", new GeoPoint(40.7950, -77.8630));
    }

    public GeocodingService(Context context) {
        this.context = context.getApplicationContext();
        this.geocoder = new Geocoder(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    private GeoPoint getFallbackCoordinate(String address) {
        String normalized = address.toLowerCase().trim();

        // Try exact match
        if (FALLBACK_COORDS.containsKey(normalized)) {
            return FALLBACK_COORDS.get(normalized);
        }

        // Try partial match
        for (Map.Entry<String, GeoPoint> entry : FALLBACK_COORDS.entrySet()) {
            if (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)) {
                return entry.getValue();
            }
        }

        // Default to Penn State main campus
        return new GeoPoint(40.7982, -77.8599);
    }

    public void getGeoPointsFromAddresses(String fromAddress, String toAddress,
                                          BatchGeocodingCallback callback) {
        executorService.execute(() -> {
            try {
                // Check if Geocoder is available
                if (!Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available, using fallback coordinates");
                    useFallbackCoordinates(fromAddress, toAddress, callback);
                    return;
                }

                // Try geocoding 'from' address
                GeoPoint startGeoPoint = null;
                try {
                    List<Address> fromAddresses = geocoder.getFromLocationName(fromAddress, 1);
                    if (fromAddresses != null && !fromAddresses.isEmpty()) {
                        startGeoPoint = new GeoPoint(
                                fromAddresses.get(0).getLatitude(),
                                fromAddresses.get(0).getLongitude()
                        );
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Geocoding failed for 'from' address, using fallback", e);
                }

                // Fallback if geocoding failed
                if (startGeoPoint == null) {
                    startGeoPoint = getFallbackCoordinate(fromAddress);
                    Log.d(TAG, "Using fallback for 'from': " + fromAddress + " -> " + startGeoPoint);
                }

                // Try geocoding 'to' address
                GeoPoint endGeoPoint = null;
                try {
                    List<Address> toAddresses = geocoder.getFromLocationName(toAddress, 1);
                    if (toAddresses != null && !toAddresses.isEmpty()) {
                        endGeoPoint = new GeoPoint(
                                toAddresses.get(0).getLatitude(),
                                toAddresses.get(0).getLongitude()
                        );
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Geocoding failed for 'to' address, using fallback", e);
                }

                // Fallback if geocoding failed
                if (endGeoPoint == null) {
                    endGeoPoint = getFallbackCoordinate(toAddress);
                    Log.d(TAG, "Using fallback for 'to': " + toAddress + " -> " + endGeoPoint);
                }

                // Return results
                final GeoPoint finalStart = startGeoPoint;
                final GeoPoint finalEnd = endGeoPoint;
                mainHandler.post(() -> callback.onSuccess(finalStart, finalEnd));

            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in geocoding, using fallback", e);
                useFallbackCoordinates(fromAddress, toAddress, callback);
            }
        });
    }

    private void useFallbackCoordinates(String fromAddress, String toAddress,
                                        BatchGeocodingCallback callback) {
        GeoPoint startGeoPoint = getFallbackCoordinate(fromAddress);
        GeoPoint endGeoPoint = getFallbackCoordinate(toAddress);

        Log.d(TAG, "Using fallback coordinates: " + fromAddress + " -> " + startGeoPoint);
        Log.d(TAG, "Using fallback coordinates: " + toAddress + " -> " + endGeoPoint);

        mainHandler.post(() -> callback.onSuccess(startGeoPoint, endGeoPoint));
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public interface BatchGeocodingCallback {
        void onSuccess(GeoPoint startLocation, GeoPoint endLocation);
        void onError(String error);
    }
}