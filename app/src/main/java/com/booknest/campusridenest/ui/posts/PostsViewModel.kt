package com.booknest.campusridenest.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknest.campusridenest.data.repo.OfferRepository
import com.booknest.campusridenest.data.repo.RequestRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T): UiState<T>
    data class Error(val message: String): UiState<Nothing>
    object Empty : UiState<Nothing>
}

class PostsViewModel(
    private val offerRepo: OfferRepository = OfferRepository(),
    private val requestRepo: RequestRepository = RequestRepository()
): ViewModel() {

    private val _browse = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val browse: StateFlow<UiState<List<PostUi>>> = _browse

    private val _mine = MutableStateFlow<UiState<List<PostUi>>>(UiState.Loading)
    val mine: StateFlow<UiState<List<PostUi>>> = _mine

    init {
        loadBrowse()
        loadMine()
    }

    fun loadBrowse() {
        viewModelScope.launch {
            _browse.value = UiState.Loading
            combine(
                offerRepo.getOpenOffers(),
                requestRepo.getOpenRequests()
            ) { offers, requests ->
                val a = offers.map { it.toPostUi() }
                val b = requests.map { it.toPostUi() }
                (a + b).sortedByDescending { it.updatedAt }
            }.catch { e ->
                _browse.value = UiState.Error(e.message ?: "Failed to load")
            }.collect { list ->
                _browse.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
        }
    }

    fun loadMine() {
        viewModelScope.launch {
            _mine.value = UiState.Loading
            val uid = Firebase.auth.currentUser?.uid ?: ""
            if (uid.isEmpty()) {
                _mine.value = UiState.Empty
                return@launch
            }
            combine(
                offerRepo.getMyOffers(uid),
                requestRepo.getMyRequests(uid)
            ) { offers, requests ->
                val a = offers.map { it.toPostUi() }
                val b = requests.map { it.toPostUi() }
                (a + b).sortedByDescending { it.updatedAt }
            }.catch { e ->
                _mine.value = UiState.Error(e.message ?: "Failed to load")
            }.collect { list ->
                _mine.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
        }
    }
}
