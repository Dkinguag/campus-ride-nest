package com.booknest.campusridenest.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.booknest.campusridenest.R;
import com.booknest.campusridenest.ui.OnboardingActivity;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final int[] layouts = {
            R.layout.item_onboarding_slide_1,
            R.layout.item_onboarding_slide_2,
            R.layout.item_onboarding_slide_3
    };

    private OnboardingActivity activity;

    public OnboardingAdapter(OnboardingActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layouts[viewType], parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Layouts are already set up, no binding needed
        // But you could add dynamic content here if needed
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}