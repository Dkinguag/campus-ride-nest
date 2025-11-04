package com.booknest.campusridenest.ui.posts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostsFragment : Fragment(R.layout.fragment_posts) {

    // Use viewModels() delegate - automatically provides SavedStateHandle
    private val vm: PostsViewModel by viewModels()

    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var adapter: PostsAdapter

    private var observeJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        list = view.findViewById(R.id.list)
        empty = view.findViewById(R.id.empty)
        toggle = view.findViewById(R.id.toggle)

        // Setup RecyclerView
        adapter = PostsAdapter(
            onEdit = { /* TODO edit */ },
            onDelete = { /* TODO delete */ },
            onClick = { post -> openPostDetail(post) }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            vm.retryLoad()
        }

        // Default tab = Browse
        startObservingBrowse()

        // Switch between Browse and My Posts
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnBrowse -> {
                    vm.setCurrentTab(PostsViewModel.Tab.BROWSE)
                    startObservingBrowse()
                }
                R.id.btnMine -> {
                    vm.setCurrentTab(PostsViewModel.Tab.MINE)
                    startObservingMine()
                }
            }
        }

        // Setup filter UI
        setupFilterUI(view)
    }

    // Setup filter button and chip
    private fun setupFilterUI(view: View) {
        val btnFilter = view.findViewById<ImageButton>(R.id.btn_filter)
        val chipFilterActive = view.findViewById<Chip>(R.id.chip_filter_active)

        // Filter button click - open bottom sheet
        btnFilter?.setOnClickListener {
            val filterSheet = FilterBottomSheetFragment()
            filterSheet.show(childFragmentManager, FilterBottomSheetFragment.TAG)
        }

        // Observe filter state using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.filterState.collectLatest { filterState ->
                    chipFilterActive?.let { updateFilterChip(it, filterState) }
                }
            }
        }
    }

    // Update filter chip based on FilterState
    private fun updateFilterChip(chip: Chip, filterState: FilterState) {
        if (filterState.isActive) {
            chip.visibility = View.VISIBLE
            chip.text = "${filterState.activeCount} filter${if (filterState.activeCount > 1) "s" else ""}"
            chip.setOnCloseIconClickListener {
                vm.clearFilters()
            }
        } else {
            chip.visibility = View.GONE
        }
    }

    // Open detail screen
    private fun openPostDetail(post: PostUi) {
        val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("type", post.type)
            putExtra("from", post.from)
            putExtra("to", post.to)

            // Convert dateTime to long timestamp - handle any type
            val dateTimeLong = try {
                val dt = post.dateTime
                when {
                    dt == null -> 0L
                    dt is Long -> dt
                    dt is Number -> dt.toLong()
                    dt is String -> dt.toLongOrNull() ?: 0L
                    // Handle Firestore Timestamp by checking class name
                    dt.javaClass.simpleName == "Timestamp" -> {
                        // Use reflection to get seconds field
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

            // FIXED: Pass actual status from post, default to "open" if null
            putExtra("status", post.status ?: "open")

            // FIXED: Pass actual price from post if it's an offer
            if (post.type.equals("offer", ignoreCase = true)) {
                putExtra("price", post.price ?: 0)
            } else {
                putExtra("price", 0)
            }
        }
        startActivity(intent)
    }

    // Browse = everyone's open offers/requests
    private fun startObservingBrowse() {
        swapCollector {
            vm.loadBrowse()
            vm.browse.collectLatest { render(it) }
        }
    }

    // Mine = only my posts (offers + requests)
    private fun startObservingMine() {
        swapCollector {
            vm.loadMine()
            vm.mine.collectLatest { render(it) }
        }
    }

    // Cancels the previous collect job and starts a new one
    private fun swapCollector(block: suspend () -> Unit) {
        observeJob?.cancel()
        observeJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }

    // Render UiState<List<PostUi>> into the screen
    private fun render(state: UiState<List<PostUi>>) {
        // Hide SwipeRefresh loading if present
        view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)?.isRefreshing = false

        when (state) {
            is UiState.Loading -> {
                // Show loading indicator
                view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)?.isRefreshing = true
                adapter.submitList(emptyList())
                empty.isVisible = false
                hideError()
            }
            is UiState.Empty -> {
                adapter.submitList(emptyList())
                empty.isVisible = true
                empty.text = getString(R.string.no_posts_yet)
                hideError()
            }
            is UiState.Error -> {
                adapter.submitList(emptyList())
                empty.isVisible = false
                showError(state.message, state.canRetry)
            }
            is UiState.Success -> {
                empty.isVisible = state.data.isEmpty()
                adapter.submitList(state.data)
                hideError()
            }
        }
    }

    private fun showError(message: String, canRetry: Boolean) {
        val errorLayout = view?.findViewById<View>(R.id.errorLayout)
        val errorMessage = view?.findViewById<TextView>(R.id.errorMessage)
        val retryButton = view?.findViewById<View>(R.id.retryButton)

        errorLayout?.isVisible = true
        errorMessage?.text = message
        retryButton?.isVisible = canRetry

        retryButton?.setOnClickListener {
            vm.retryLoad()
        }
    }

    private fun hideError() {
        val errorLayout = view?.findViewById<View>(R.id.errorLayout)
        errorLayout?.isVisible = false
    }
}