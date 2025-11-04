package com.booknest.campusridenest.ui.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknest.campusridenest.data.repo.OfferRepository
import com.booknest.campusridenest.data.repo.RequestRepository
import com.booknest.campusridenest.model.RideOffer
import com.booknest.campusridenest.model.RideRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel(
    private val offerRepo: OfferRepository = OfferRepository(),
    private val requestRepo: RequestRepository = RequestRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // REMOVED SavedStateHandle parameter completely for now
) : ViewModel() {

    // Simplified filter state - no persistence for now
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Update filters
    fun updateFilters(newFilters: FilterState) {
        _filterState.value = newFilters
    }

    // Clear filters
    fun clearFilters() {
        _filterState.value = FilterState()
    }

    // ----- UI model -----
    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(
            val message: String,
            val exception: Exception? = null,
            val canRetry: Boolean = true
        ) : UiState<Nothing>()
        object Empty : UiState<Nothing>()
    }

    private val _browse = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val browse: StateFlow<UiState<List<PostUi>>> = _browse

    private val _mine = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val mine: StateFlow<UiState<List<PostUi>>> = _mine

    init {
        loadBrowse()
        loadMine()
    }

    private fun safeUpdatedAt(p: PostUi): Long = when (val v = p.updatedAt) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull() ?: 0L
        else -> 0L
    }

    // UPDATED: Browse with filter support
    fun loadBrowse() {
        viewModelScope.launch {
            try {
                _browse.value = UiState.Loading

                // NEW: Combine with filter state
                combine(
                    offerRepo.getOpenOffers(),
                    requestRepo.getOpenRequests(),
                    _filterState  // Include filter state
                ) { offers: List<RideOffer>, requests: List<RideRequest>, filters: FilterState ->
                    val a = offers.map { it.toPostUi() }
                    val b = requests.map { it.toPostUi() }

                    (a + b)
                        .filter { it.status == "open" }  // NEW: Only open posts
                        .filter { filters.matchesPost(it) }  // NEW: Apply user filters
                        .sortedByDescending { safeUpdatedAt(it) }
                }.collect { list: List<PostUi> ->
                    _browse.value = if (list.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(list)
                    }
                }
            } catch (e: Exception) {
                _browse.value = UiState.Error(
                    message = "Failed to load posts. Please check your connection.",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    fun loadMine() {
        viewModelScope.launch {
            try {
                _mine.value = UiState.Loading

                val currentUid = auth.currentUser?.uid ?: run {
                    _mine.value = UiState.Error(
                        message = "User not authenticated",
                        exception = null,
                        canRetry = false
                    )
                    return@launch
                }

                combine(
                    offerRepo.getMyOffers(currentUid),
                    requestRepo.getMyRequests(currentUid)
                ) { offers: List<RideOffer>, requests: List<RideRequest> ->
                    val a = offers.map { it.toPostUi() }
                    val b = requests.map { it.toPostUi() }
                    (a + b).sortedByDescending { safeUpdatedAt(it) }
                }.collect { list: List<PostUi> ->
                    _mine.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
            } catch (e: Exception) {
                _mine.value = UiState.Error(
                    message = "Failed to load posts. Please check your connection.",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    enum class Tab {
        BROWSE, MINE
    }

    private val _currentTab = MutableStateFlow(Tab.BROWSE)
    val currentTab: StateFlow<Tab> = _currentTab.asStateFlow()

    fun retryLoad() {
        viewModelScope.launch {
            try {
                when (_currentTab.value) {
                    Tab.BROWSE -> loadBrowse()
                    Tab.MINE -> loadMine()
                }
            } catch (e: Exception) {
                when (_currentTab.value) {
                    Tab.BROWSE -> {
                        _browse.value = UiState.Error(
                            message = "Failed to load posts. Please check your connection.",
                            exception = e,
                            canRetry = true
                        )
                    }
                    Tab.MINE -> {
                        _mine.value = UiState.Error(
                            message = "Failed to load posts. Please check your connection.",
                            exception = e,
                            canRetry = true
                        )
                    }
                }
            }
        }
    }

    fun setCurrentTab(tab: Tab) {
        _currentTab.value = tab
    }
}