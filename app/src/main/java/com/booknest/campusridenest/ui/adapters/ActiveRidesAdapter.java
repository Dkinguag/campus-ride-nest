package com.booknest.campusridenest.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.ui.ActiveRidesActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActiveRidesAdapter extends RecyclerView.Adapter<ActiveRidesAdapter.ViewHolder> {

    private List<ActiveRidesActivity.ActiveRide> rides;

    public ActiveRidesAdapter(List<ActiveRidesActivity.ActiveRide> rides) {
        this.rides = rides;
    }

    public void updateRides(List<ActiveRidesActivity.ActiveRide> newRides) {
        this.rides = newRides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_ride, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveRidesActivity.ActiveRide ride = rides.get(position);
        holder.bind(ride);
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute;
        private final TextView tvRideTime;
        private final TextView tvRiderName;
        private final TextView tvPickupLocation;
        private final Button btnViewMap;
        private final Button btnContactRider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvActiveRideRoute);
            tvRideTime = itemView.findViewById(R.id.tvActiveRideTime);
            tvRiderName = itemView.findViewById(R.id.tvRiderName);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            btnViewMap = itemView.findViewById(R.id.btnViewPickupMap);
            btnContactRider = itemView.findViewById(R.id.btnContactRider);
        }

        public void bind(ActiveRidesActivity.ActiveRide ride) {
            // Route
            tvRoute.setText(ride.from + " → " + ride.to);

            // Time
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            tvRideTime.setText("⏰ " + sdf.format(new Date(ride.timeMillis)));

            // Rider
            tvRiderName.setText("👤 Rider: " + ride.riderName);

            // Pickup
            tvPickupLocation.setText("📍 Pick up at: " + ride.pickupLocation);

            // View on Map button
            btnViewMap.setOnClickListener(v -> {
                // Fetch pickup coordinates from request
                FirebaseFirestore.getInstance()
                        .collection("requests")
                        .document(ride.requestId)  // FIXED: Use requestId from ride
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (!doc.exists()) {
                                Toast.makeText(itemView.getContext(),
                                        "Could not load pickup location",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            GeoPoint pickupGeo = doc.getGeoPoint("pickupLocation");
                            if (pickupGeo != null) {
                                double lat = pickupGeo.getLatitude();
                                double lon = pickupGeo.getLongitude();
                                String uri = "geo:" + lat + "," + lon + "?q=" + lat + "," + lon;
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                intent.setPackage("com.google.android.apps.maps");

                                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                                    itemView.getContext().startActivity(intent);
                                } else {
                                    Toast.makeText(itemView.getContext(),
                                            "Google Maps not installed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(itemView.getContext(),
                                        "Pickup location not available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(itemView.getContext(),
                                    "Error loading location: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            });

            // Contact Rider button
            btnContactRider.setOnClickListener(v -> {
                // Fetch rider email
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(ride.riderId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String email = doc.getString("email");
                            if (email != null) {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                emailIntent.setData(Uri.parse("mailto:" + email));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Campus Ride Nest - Pickup Coordination");
                                itemView.getContext().startActivity(Intent.createChooser(emailIntent, "Contact Rider"));
                            }
                        });
            });
        }
    }
}