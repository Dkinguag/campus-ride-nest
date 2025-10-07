package com.booknest.campusridenest.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknest.campusridenest.data.repo.OfferRepository
import com.booknest.campusridenest.data.repo.RequestRepository
import com.booknest.campusridenest.model.RideOffer
import com.booknest.campusridenest.model.RideRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import com.booknest.campusridenest.ui.posts.toPostUi

class PostsViewModel(
    private val offerRepo: OfferRepository = OfferRepository(),
    private val requestRepo: RequestRepository = RequestRepository()
) : ViewModel() {

    // ----- UI model -----
    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        object Empty : UiState<Nothing>()
        data class Error(val message: String) : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
    }

    private val _browse = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val browse: StateFlow<UiState<List<PostUi>>> = _browse

    private val _mine = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val mine: StateFlow<UiState<List<PostUi>>> = _mine

    init {
        loadBrowse()
        loadMine()
    }

    // for stable sort even if updatedAt is String/Long/whatever
    private fun safeUpdatedAt(p: PostUi): Long = when (val v = p.updatedAt) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull() ?: 0L
        else -> 0L
    }

    fun loadBrowse() {
        viewModelScope.launch {
            combine(
                offerRepo.getOpenOffers(),
                requestRepo.getOpenRequests()
            ) { offers: List<RideOffer>, requests: List<RideRequest> ->
                val a = offers.map { it.toPostUi() }
                val b = requests.map { it.toPostUi() }
                (a + b).sortedByDescending { safeUpdatedAt(it) }
            }
                .catch { e -> _browse.value = UiState.Error(e.message ?: "Failed to load") }
                .collect { list: List<PostUi> ->
                    _browse.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
        }
    }

    fun loadMine() {
        viewModelScope.launch {
            _mine.value = UiState.Loading

            val uid = Firebase.auth.currentUser?.uid ?: ""
            if (uid.isBlank()) {
                _mine.value = UiState.Empty
                return@launch
            }

            combine(
                offerRepo.getMyOffers(uid),
                requestRepo.getMyRequests(uid)
            ) { offers: List<RideOffer>, requests: List<RideRequest> ->
                val a = offers.map { it.toPostUi() }
                val b = requests.map { it.toPostUi() }
                (a + b).sortedByDescending { safeUpdatedAt(it) }
            }
                .catch { e -> _mine.value = UiState.Error(e.message ?: "Failed to load") }
                .collect { list: List<PostUi> ->
                    _mine.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
        }
    }
}
