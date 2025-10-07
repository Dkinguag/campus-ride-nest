package com.booknest.campusridenest.ui.posts

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booknest.campusridenest.R
import com.booknest.campusridenest.ui.posts.PostsViewModel.UiState
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class PostsFragment : Fragment(R.layout.fragment_posts) {

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

        // Recycler
        adapter = PostsAdapter(
            onEdit = { /* TODO edit */ },
            onDelete = { /* TODO delete */ },
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        // Default tab = Browse
        startObservingBrowse()

        // Switch between Browse and My Posts
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnBrowse -> startObservingBrowse()
                R.id.btnMine   -> startObservingMine()
            }
        }
    }

    /** Browse = everyone's open offers/requests */
    private fun startObservingBrowse() {
        swapCollector {
            vm.loadBrowse() // optional "pull to refresh" trigger, safe to call
            vm.browse.collectLatest { render(it) }
        }
    }

    /** Mine = only my posts (offers + requests) */
    private fun startObservingMine() {
        swapCollector {
            vm.loadMine()
            vm.mine.collectLatest { render(it) }
        }
    }

    /**
     * Cancels the previous collect job and starts a new one scoped to viewLifecycle
     * so it automatically stops/starts with STARTED state.
     */
    private fun swapCollector(block: suspend () -> Unit) {
        observeJob?.cancel()
        observeJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                block()
            }
        }
    }

    /** Render UiState<List<PostUi>> into the screen */
    private fun render(state: UiState<List<PostUi>>) {
        when (state) {
            is UiState.Loading -> {
                adapter.submitList(emptyList())
                empty.isVisible = false
            }
            is UiState.Empty -> {
                adapter.submitList(emptyList())
                empty.isVisible = true
                empty.text = getString(R.string.no_posts_yet) // make sure strings.xml has this
            }
            is UiState.Error -> {
                adapter.submitList(emptyList())
                empty.isVisible = true
                empty.text = state.message
            }
            is UiState.Success -> {
                empty.isVisible = state.data.isEmpty()
                adapter.submitList(state.data)
            }
        }
    }
}
