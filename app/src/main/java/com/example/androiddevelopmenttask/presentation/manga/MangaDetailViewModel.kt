package com.example.androiddevelopmenttask.presentation.manga

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.usecase.manga.GetMangaDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val getMangaDetailUseCase: GetMangaDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _mangaDetailState = MutableStateFlow<MangaDetailState>(MangaDetailState.Loading)
    val mangaDetailState: StateFlow<MangaDetailState> = _mangaDetailState.asStateFlow()

    private val TAG = "MangaDetailViewModel"

    init {
        // Get mangaId as Int since it's defined as NavType.IntType in Navigation.kt
        val mangaId = savedStateHandle.get<Int>("mangaId")
        if (mangaId != null) {
            Log.d(TAG, "Loading manga detail for ID: $mangaId")
            loadMangaDetail(mangaId)
        } else {
            Log.e(TAG, "No manga ID provided")
            _mangaDetailState.value = MangaDetailState.Error("No manga ID provided")
        }
    }

    private fun loadMangaDetail(mangaId: Int) {
        viewModelScope.launch {
            _mangaDetailState.value = MangaDetailState.Loading

            try {
                when (val result = getMangaDetailUseCase(mangaId)) {
                    is Result.Success -> {
                        _mangaDetailState.value = MangaDetailState.Success(result.data)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error loading manga detail: ${result.message}")
                        _mangaDetailState.value = MangaDetailState.Error(result.message)
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading manga detail: ${e.message}")
                _mangaDetailState.value = MangaDetailState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class MangaDetailState {
    object Loading : MangaDetailState()
    data class Success(val manga: Manga) : MangaDetailState()
    data class Error(val message: String) : MangaDetailState()
}
