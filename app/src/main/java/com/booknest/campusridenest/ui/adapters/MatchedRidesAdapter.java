package com.booknest.campusridenest.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.model.RideMatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchedRidesAdapter extends RecyclerView.Adapter<MatchedRidesAdapter.ViewHolder> {

    private final List<RideMatch> matches;
    private final OnMatchClickListener listener;

    // NEW: Click listener interface
    public interface OnMatchClickListener {
        void onMatchClick(RideMatch match);
    }

    // UPDATED: Constructor with listener
    public MatchedRidesAdapter(List<RideMatch> matches, OnMatchClickListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideMatch match = matches.get(position);
        holder.bind(match, listener);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute;
        private final TextView tvDateTime;
        private final TextView tvPrice;
        private final TextView tvMatchScore;
        private final TextView tvDistance;
        private final TextView tvCompatibility;
        private final com.google.android.material.button.MaterialButton btnConfirmRide;  // NEW

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvCompatibility = itemView.findViewById(R.id.tvCompatibility);
            btnConfirmRide = itemView.findViewById(R.id.btnConfirmRide);  // NEW
        }

        public void bind(RideMatch match, OnMatchClickListener listener) {
            tvRoute.setText(match.offer.from + " → " + match.offer.to);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date(match.offer.timeMillis)));

            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", match.offer.pricePerSeat));

            int score = (int) match.matchScore;
            tvMatchScore.setText(score + "% Match");

            String distanceText = String.format(Locale.getDefault(),
                    "%.1f km pickup • %.1f km dropoff",
                    match.pickupDistanceKm, match.dropoffDistanceKm);
            tvDistance.setText(distanceText);

            tvCompatibility.setText(match.compatibilityReason);

            // NEW: Button click
            btnConfirmRide.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(match);
                }
            });
        }
    }
}