package com.example.androiddevelopmenttask.presentation.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import com.example.androiddevelopmenttask.domain.usecase.manga.GetMangaListUseCase
import com.example.androiddevelopmenttask.util.DatabaseInspector
import com.example.androiddevelopmenttask.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMangaListUseCase: GetMangaListUseCase,
    private val mangaRepository: MangaRepository,
    private val databaseInspector: DatabaseInspector,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _mangaListState = MutableStateFlow<MangaListState>(MangaListState.Loading)
    val mangaListState: StateFlow<MangaListState> = _mangaListState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20
    private var isLastPage = false

    private val TAG = "HomeViewModel"

    // Track network state to detect when connectivity is restored
    private var wasNetworkAvailable = false

    init {
        // Load cached data first, then refresh from network
        loadCachedData()

        // Start monitoring network connectivity
        monitorNetworkConnectivity()
    }

    private fun loadCachedData() {
        viewModelScope.launch {
            try {
                mangaRepository.getLocalMangaList().collectLatest { cachedList ->
                    if (cachedList.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${cachedList.size} manga from cache")
                        _mangaListState.value = MangaListState.Success(cachedList)
                    } else {
                        // If cache is empty, load from network
                        Log.d(TAG, "Cache is empty, loading from network")
                        loadMangaList()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached data: ${e.message}")
                // If there's an error loading from cache, try network
                loadMangaList()
            }
        }
    }

    fun loadMangaList() {
        if (isLastPage && _mangaListState.value is MangaListState.Success) return

        viewModelScope.launch {
            val currentState = _mangaListState.value
            val currentMangaList = if (currentState is MangaListState.Success) {
                currentState.mangaList
            } else {
                emptyList()
            }

            if (currentPage == 1) {
                _mangaListState.value = MangaListState.Loading
            } else {
                _mangaListState.value = MangaListState.LoadingMore(currentMangaList)
            }

            // Log network status for debugging
            NetworkUtils.logNetworkInfo(context)
            val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
            Log.d(TAG, "Network available before API call: $isNetworkAvailable")

            when (val result = getMangaListUseCase(currentPage, pageSize)) {
                is Result.Success -> {
                    val newList = if (currentPage == 1) {
                        result.data
                    } else {
                        currentMangaList + result.data
                    }

                    isLastPage = result.data.isEmpty()
                    if (!isLastPage) currentPage++

                    _mangaListState.value = MangaListState.Success(newList)
                }
                is Result.Error -> {
                    Log.e(TAG, "Error loading manga list: ${result.message}")

                    val errorMessage = if (!isNetworkAvailable) {
                        "No internet connection. Please check your network settings and try again."
                    } else {
                        result.message
                    }

                    _mangaListState.value = if (currentMangaList.isEmpty()) {
                        MangaListState.Error(errorMessage)
                    } else {
                        // Show a success state with existing data but log the error
                        Log.d(TAG, "Showing cached data despite error: $errorMessage")
                        MangaListState.Success(currentMangaList)
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun refreshMangaList() {
        Log.d(TAG, "Refreshing manga list - resetting to page 1")
        currentPage = 1
        isLastPage = false

        // First check if we need to refresh the cache
        val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
        if (isNetworkAvailable) {
            // If network is available, refresh the cache first
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Network available, refreshing cache before loading UI")
                    val refreshResult = mangaRepository.refreshCacheIfOnline()

                    when (refreshResult) {
                        is Result.Success -> {
                            if (refreshResult.data) {
                                Log.d(TAG, "Cache refresh successful, now loading UI")
                            } else {
                                Log.d(TAG, "Cache refresh skipped, loading UI with existing data")
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error refreshing cache: ${refreshResult.message}")
                        }
                        else -> {}
                    }

                    // Load the UI with the latest data (whether refresh succeeded or not)
                    loadMangaList()
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during cache refresh: ${e.message}")
                    // If refresh fails, still try to load the UI
                    loadMangaList()
                }
            }
        } else {
            // If network is not available, just load from cache
            Log.d(TAG, "Network not available, loading from cache only")
            loadMangaList()
        }
    }

    // Add a function to log detailed diagnostic information
    fun logDiagnosticInfo() {
        viewModelScope.launch {
            Log.d(TAG, "=== DIAGNOSTIC INFO ===")
            Log.d(TAG, "Current page: $currentPage")
            Log.d(TAG, "Is last page: $isLastPage")
            Log.d(TAG, "Current state: ${_mangaListState.value::class.simpleName}")

            // Check network status
            val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
            Log.d(TAG, "Network available: $isNetworkAvailable")
            NetworkUtils.logNetworkInfo(context)

            // Inspect database
            databaseInspector.inspectMangaDatabase()

            // Log current manga list size
            if (_mangaListState.value is MangaListState.Success) {
                val mangaList = (_mangaListState.value as MangaListState.Success).mangaList
                Log.d(TAG, "Current manga list size: ${mangaList.size}")
            }

            Log.d(TAG, "=== END DIAGNOSTIC INFO ===")
        }
    }

    fun inspectDatabase() {
        Log.d(TAG, "Inspecting database...")
        databaseInspector.inspectMangaDatabase()
    }

    /**
     * Monitors network connectivity and refreshes the cache when internet is restored
     */
    private fun monitorNetworkConnectivity() {
        viewModelScope.launch {
            // Check network status periodically
            while (true) {
                try {
                    val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)

                    // If network was previously unavailable but is now available, refresh the cache
                    if (!wasNetworkAvailable && isNetworkAvailable) {
                        Log.d(TAG, "Network connectivity restored, refreshing cache")
                        refreshCacheInBackground()
                    }

                    // Update the previous network state
                    wasNetworkAvailable = isNetworkAvailable

                    // Wait before checking again (every 10 seconds)
                    kotlinx.coroutines.delay(10000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error monitoring network connectivity: ${e.message}")
                    // Wait before trying again
                    kotlinx.coroutines.delay(30000)
                }
            }
        }
    }

    /**
     * Refreshes the cache in the background without updating the UI
     */
    private fun refreshCacheInBackground() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting background cache refresh")
                val result = mangaRepository.refreshCacheIfOnline()

                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            Log.d(TAG, "Background cache refresh completed successfully")
                        } else {
                            Log.d(TAG, "Background cache refresh skipped (network unavailable)")
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error during background cache refresh: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during background cache refresh: ${e.message}")
            }
        }
    }
}

sealed class MangaListState {
    object Loading : MangaListState()
    data class LoadingMore(val mangaList: List<Manga>) : MangaListState()
    data class Success(val mangaList: List<Manga>) : MangaListState()
    data class Error(val message: String) : MangaListState()
}
