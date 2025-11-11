package com.booknest.campusridenest.ui.posts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booknest.campusridenest.R
import com.booknest.campusridenest.ui.posts.PostsViewModel.UiState
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val vm: PostsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var filterButton: ImageButton
    private lateinit var filterChipsGroup: ChipGroup
    private lateinit var adapter: PostsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views matching fragment_posts.xml
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyState = view.findViewById(R.id.emptyState)
        filterButton = view.findViewById(R.id.filterButton)
        filterChipsGroup = view.findViewById(R.id.filterChipsGroup)

        // Setup RecyclerView
        // FIXED: Positional parameters in correct order (onClick, onEdit, onDelete)
        adapter = PostsAdapter(
            { post -> openPostDetail(post) },  // onClick - first parameter
            { /* TODO: edit */ },              // onEdit - second parameter
            { /* TODO: delete */ }             // onDelete - third parameter
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Setup FAB if needed
        val createFab = view.findViewById<FloatingActionButton>(R.id.createFab)
        createFab?.setOnClickListener {
            // TODO: Navigate to create post screen
        }

        // Setup filter button
        setupFilterUI()

        // Start observing posts
        startObserving()
    }

    private fun setupFilterUI() {
        // Filter button click - open bottom sheet
        filterButton.setOnClickListener {
            val filterSheet = FilterBottomSheetFragment()
            filterSheet.show(childFragmentManager, FilterBottomSheetFragment.TAG)
        }

        // Observe filter state using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.filterState.collectLatest { filterState ->
                    updateFilterUI(filterState)
                }
            }
        }
    }

    private fun updateFilterUI(filterState: FilterState) {
        // Update filter chips
        filterChipsGroup.removeAllViews()

        if (filterState.isActive) {
            filterChipsGroup.visibility = View.VISIBLE

            // Add chips for each active filter
            filterState.origin?.let { origin ->
                addFilterChip(origin) {
                    // Clear origin filter
                    vm.clearFilters() // Or specific clear method if available
                }
            }

            filterState.destination?.let { destination ->
                addFilterChip(destination) {
                    // Clear destination filter
                    vm.clearFilters()
                }
            }

            filterState.dateRange?.let { dateRange ->
                val label = when (dateRange) {
                    DateRange.ThisWeek -> "This Week"
                    DateRange.NextWeek -> "Next Week"
                    is DateRange.Custom -> "Custom Range"
                    else -> "Date Filter"
                }
                addFilterChip(label) {
                    // Clear date filter
                    vm.clearFilters()
                }
            }
        } else {
            filterChipsGroup.visibility = View.GONE
        }
    }

    private fun addFilterChip(text: String, onClose: () -> Unit) {
        val chip = com.google.android.material.chip.Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            onClose()
        }
        chip.contentDescription = "Remove $text filter"
        filterChipsGroup.addView(chip)
    }

    private fun startObserving() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.loadBrowse()
                vm.browse.collectLatest { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: UiState<List<PostUi>>) {
        when (state) {
            is UiState.Loading -> {
                adapter.submitList(emptyList())
                emptyState.isVisible = false
                recyclerView.isVisible = true
            }
            is UiState.Empty -> {
                adapter.submitList(emptyList())
                emptyState.isVisible = true
                recyclerView.isVisible = false

                // ACCESSIBILITY: Announce no results if filters are active
                val filterState = vm.filterState.value
                if (filterState.isActive) {
                    announceNoResults()
                }
            }
            is UiState.Error -> {
                adapter.submitList(emptyList())
                emptyState.isVisible = true
                recyclerView.isVisible = false
                // TODO: Show error message in empty state
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    emptyState.isVisible = true
                    recyclerView.isVisible = false

                    // ACCESSIBILITY: Announce no results if filters are active
                    val filterState = vm.filterState.value
                    if (filterState.isActive) {
                        announceNoResults()
                    }
                } else {
                    emptyState.isVisible = false
                    recyclerView.isVisible = true
                }
                adapter.submitList(state.data)
            }
        }
    }

    private fun openPostDetail(post: PostUi) {
        val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("type", post.type)
            putExtra("from", post.from)
            putExtra("to", post.to)

            // Convert dateTime to long timestamp
            val dateTimeLong = try {
                val dt = post.dateTime
                when {
                    dt == null -> 0L
                    dt is Long -> dt
                    dt is Number -> dt.toLong()
                    dt is String -> dt.toLongOrNull() ?: 0L
                    // Handle Firestore Timestamp
                    dt.javaClass.simpleName == "Timestamp" -> {
                        val secondsField = dt.javaClass.getField("seconds")
                        (secondsField.get(dt) as Long) * 1000
                    }
                    else -> 0L
                }
            } catch (e: Exception) {
                android.util.Log.e("PostsFragment", "Error converting dateTime: ${e.message}")
                0L
            }
            putExtra("dateTime", dateTimeLong)

            putExtra("seats", post.seats ?: 0)
            putExtra("ownerUid", post.ownerUid)
            putExtra("status", post.status ?: "open")

            if (post.type.equals("offer", ignoreCase = true)) {
                putExtra("price", post.price ?: 0)
            } else {
                putExtra("price", 0)
            }
        }
        startActivity(intent)
    }
    // ============ ACCESSIBILITY ANNOUNCEMENTS (Commit 4) ============

    /**
     * Announce when filters are applied
     * Example: "2 filters applied. 12 posts found."
     */
    private fun announceFiltersApplied(filterCount: Int, postCount: Int) {
        val announcement = getString(R.string.announce_filters_applied, filterCount, postCount)
        view?.announceForAccessibility(announcement)
    }

    /**
     * Announce when filters are cleared
     * Example: "All filters cleared. Showing all posts."
     */
    private fun announceFiltersCleared() {
        val announcement = getString(R.string.announce_filters_cleared)
        view?.announceForAccessibility(announcement)
    }

    /**
     * Announce when no results match filters
     * Need to add this string to strings.xml first
     */
    private fun announceNoResults() {
        val announcement = "No posts found matching your filters. Try adjusting your search."
        view?.announceForAccessibility(announcement)
    }
}