package com.booknest.campusridenest.ui.posts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
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

        // Initialize views
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
    }

    //Open detail screen
    private fun openPostDetail(post: PostUi) {
        val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
            putExtra("POST_ID", post.id)
            putExtra("TYPE", post.type)
            putExtra("FROM", post.from)
            putExtra("TO", post.to)
            putExtra("DATE_TIME", when (val dt = post.dateTime) {
                is String -> dt.toLongOrNull()?.toShortDateTime() ?: dt
                is Number -> dt.toLong().toShortDateTime()
                else -> dt?.toString() ?: ""
            })
            putExtra("SEATS", post.seats ?: 0)
            putExtra("OWNER_UID", post.ownerUid)
        }
        startActivity(intent)
    }

    // Browse = everyone's open offers/requests
    private fun startObservingBrowse() {
        swapCollector {
            vm.loadBrowse() // optional "pull to refresh" trigger, safe to call
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

    // Cancels the previous collect job and starts a new one scoped to viewLifecycle so it automatically stops/starts with STARTED state.

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