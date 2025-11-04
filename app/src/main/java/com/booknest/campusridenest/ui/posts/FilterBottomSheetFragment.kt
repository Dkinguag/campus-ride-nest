package com.booknest.campusridenest.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import com.booknest.campusridenest.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: PostsViewModel

    // UI Components
    private lateinit var etOrigin: EditText
    private lateinit var etDestination: EditText
    private lateinit var rgDateRange: RadioGroup
    private lateinit var rbThisWeek: RadioButton
    private lateinit var rbNextWeek: RadioButton
    private lateinit var rbNoDate: RadioButton
    private lateinit var btnApply: Button
    private lateinit var btnClear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[PostsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etOrigin = view.findViewById(R.id.et_filter_origin)
        etDestination = view.findViewById(R.id.et_filter_destination)
        rgDateRange = view.findViewById(R.id.rg_date_range)
        rbThisWeek = view.findViewById(R.id.rb_this_week)
        rbNextWeek = view.findViewById(R.id.rb_next_week)
        rbNoDate = view.findViewById(R.id.rb_no_date)
        btnApply = view.findViewById(R.id.btn_apply_filters)
        btnClear = view.findViewById(R.id.btn_clear_filters)

        // Load existing filters
        loadExistingFilters()

        // Setup click listeners
        setupClickListeners()
    }

    private fun loadExistingFilters() {
        val currentFilters = viewModel.filterState.value

        // Pre-populate origin
        currentFilters.origin?.let {
            etOrigin.setText(it)
        }

        // Pre-populate destination
        currentFilters.destination?.let {
            etDestination.setText(it)
        }

        // Pre-select date range radio button
        when (currentFilters.dateRange) {
            is DateRange.ThisWeek -> rbThisWeek.isChecked = true
            is DateRange.NextWeek -> rbNextWeek.isChecked = true
            else -> rbNoDate.isChecked = true
        }
    }

    private fun setupClickListeners() {
        // Apply filters button
        btnApply.setOnClickListener {
            applyFilters()
        }

        // Clear filters button
        btnClear.setOnClickListener {
            viewModel.clearFilters()
            dismiss()
        }
    }

    private fun applyFilters() {
        // Get filter values
        val origin = etOrigin.text.toString().trim().takeIf { it.isNotBlank() }
        val destination = etDestination.text.toString().trim().takeIf { it.isNotBlank() }

        // Get selected date range
        val dateRange = when (rgDateRange.checkedRadioButtonId) {
            R.id.rb_this_week -> DateRange.ThisWeek
            R.id.rb_next_week -> DateRange.NextWeek
            else -> null
        }

        // Create new FilterState
        val newFilters = FilterState(
            origin = origin,
            destination = destination,
            dateRange = dateRange
        )

        // Update ViewModel
        viewModel.updateFilters(newFilters)

        // Dismiss bottom sheet
        dismiss()
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}