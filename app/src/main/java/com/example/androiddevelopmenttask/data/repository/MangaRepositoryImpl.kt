package com.example.androiddevelopmenttask.data.repository

import android.content.Context
import android.util.Log
import com.example.androiddevelopmenttask.data.api.MangaApiService
import com.example.androiddevelopmenttask.data.api.model.ChapterDto
import com.example.androiddevelopmenttask.data.db.dao.MangaDao
import com.example.androiddevelopmenttask.data.db.entity.MangaEntity
import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import com.example.androiddevelopmenttask.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val mangaApiService: MangaApiService,
    private val mangaDao: MangaDao,
    @ApplicationContext private val context: Context
) : MangaRepository {

    private val TAG = "MangaRepositoryImpl"

    override suspend fun getMangaList(page: Int, pageSize: Int): Result<List<Manga>> {
        return try {
            // Log network status for debugging
            NetworkUtils.logNetworkInfo(context)

            // Check if network is available
            val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
            Log.d(TAG, "Network available: $isNetworkAvailable")

            if (isNetworkAvailable) {
                try {
                    // Try to fetch from network first
                    Log.d(TAG, "Fetching manga list from API for page $page with size $pageSize")
                    try {
                        // Log detailed request information
                        Log.d(TAG, "Making API request to: mangaApiService.getMangaList($page, $pageSize)")

                        val response = mangaApiService.getMangaList(page, pageSize)
                        Log.d(TAG, "API Response: status=${response.status}, message=${response.message}, total items=${response.totalItems}")

                        if (response.data.isEmpty()) {
                            Log.w(TAG, "API returned empty data list despite successful response")
                        }

                        val mangaList = response.data.map { it.toManga() }
                        Log.d(TAG, "Mapped ${mangaList.size} manga from API response")

                        // Save to database
                        val mangaEntities = mangaList.map { MangaEntity.fromManga(it, page) }
                        mangaDao.clearAndInsertForPage(page, mangaEntities)
                        Log.d(TAG, "Saved ${mangaEntities.size} manga to database for page $page")

                        // Only return mock data if the API returned an empty list
                        if (mangaList.isEmpty()) {
                            Log.d(TAG, "API returned empty list, using mock data instead")
                            val mockData = createMockMangaData(page, pageSize)
                            val mockEntities = mockData.map { MangaEntity.fromManga(it, page) }
                            mangaDao.clearAndInsertForPage(page, mockEntities)
                            Result.Success(mockData)
                        } else {
                            Result.Success(mangaList)
                        }
                    } catch (apiException: Exception) {
                        Log.e(TAG, "API error: ${apiException.message}", apiException)
                        Log.e(TAG, "API error details: ${apiException.javaClass.simpleName}", apiException)

                        // Check if we have data in the database before using mock data
                        val dbData = loadFromDatabaseDirectly(page)
                        if (dbData.isNotEmpty()) {
                            Log.d(TAG, "Using ${dbData.size} items from database after API failure")
                            return Result.Success(dbData)
                        }

                        // If API fails and no database data, try to use mock data as a fallback
                        try {
                            Log.d(TAG, "Attempting to use mock data as fallback")
                            val mockData = createMockMangaData(page, pageSize)

                            // Save mock data to database
                            val mockEntities = mockData.map { MangaEntity.fromManga(it, page) }
                            mangaDao.clearAndInsertForPage(page, mockEntities)
                            Log.d(TAG, "Saved ${mockEntities.size} mock manga to database for page $page")

                            Result.Success(mockData)
                        } catch (mockException: Exception) {
                            Log.e(TAG, "Mock data fallback failed: ${mockException.message}", mockException)
                            // If mock data fails too, try to load from database
                            loadFromDatabase(page)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error: ${e.message}", e)
                    // API error, try to load from database
                    loadFromDatabase(page)
                }
            } else {
                Log.d(TAG, "No network connection, loading from database")
                loadFromDatabase(page)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting manga list: ${e.message}", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun loadFromDatabase(page: Int): Result<List<Manga>> {
        // Check if we have cached data first
        var cachedList = emptyList<Manga>()
        try {
            val localMangas = mangaDao.getMangasByPage(page)

            // Collect the first emission from the flow
            localMangas.collect { entities ->
                cachedList = entities.map { it.toManga() }
                Log.d(TAG, "Loaded ${entities.size} manga from database for page $page")
            }
        } catch (dbException: Exception) {
            Log.e(TAG, "Database error: ${dbException.message}")
        }

        // If we have local data, return it
        if (cachedList.isNotEmpty()) {
            return Result.Success(cachedList)
        } else {
            // If we don't have any data at all, try to get all manga from database
            try {
                var allManga = emptyList<Manga>()
                mangaDao.getAllMangas().collect { entities ->
                    allManga = entities.map { it.toManga() }
                    Log.d(TAG, "Loaded ${entities.size} manga from entire database")
                }

                return if (allManga.isNotEmpty()) {
                    Result.Success(allManga)
                } else {
                    Result.Error("No internet connection and no cached data available")
                }
            } catch (allDbException: Exception) {
                Log.e(TAG, "Error loading all manga: ${allDbException.message}")
                return Result.Error("Failed to load manga data. Pull to refresh.")
            }
        }
    }

    /**
     * Directly load manga from database without wrapping in Result
     * Used for internal checks before falling back to mock data
     */
    private suspend fun loadFromDatabaseDirectly(page: Int): List<Manga> {
        return try {
            Log.d(TAG, "Directly loading manga from database for page $page")
            var cachedList = emptyList<Manga>()

            mangaDao.getMangasByPage(page).collect { entities ->
                cachedList = entities.map { it.toManga() }
                Log.d(TAG, "Directly loaded ${cachedList.size} manga from database for page $page")
            }

            cachedList
        } catch (e: Exception) {
            Log.e(TAG, "Error directly loading from database: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getLatestManga(): Result<List<Manga>> {
        return try {
            // Check if network is available
            val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
            Log.d(TAG, "Network available for latest manga: $isNetworkAvailable")

            if (isNetworkAvailable) {
                try {
                    Log.d(TAG, "Fetching latest manga from API")
                    try {
                        val response = mangaApiService.getLatestManga()
                        val mangaList = response.data.map { it.toManga() }
                        Log.d(TAG, "Fetched ${mangaList.size} latest manga from API")

                        // Save to database with a special page number (e.g., -1 for latest)
                        val mangaEntities = mangaList.map { MangaEntity.fromManga(it, -1) }
                        mangaDao.clearAndInsertForPage(-1, mangaEntities)
                        Log.d(TAG, "Saved ${mangaEntities.size} latest manga to database")

                        Result.Success(mangaList)
                    } catch (apiException: Exception) {
                        Log.e(TAG, "API error getting latest manga: ${apiException.message}", apiException)

                        // If API fails, try to use mock data as a fallback
                        try {
                            Log.d(TAG, "Attempting to use mock latest manga data as fallback")
                            val mockData = createMockLatestManga()

                            // Save mock data to database
                            val mockEntities = mockData.map { MangaEntity.fromManga(it, -1) }
                            mangaDao.clearAndInsertForPage(-1, mockEntities)
                            Log.d(TAG, "Saved ${mockEntities.size} mock latest manga to database")

                            Result.Success(mockData)
                        } catch (mockException: Exception) {
                            Log.e(TAG, "Mock latest data fallback failed: ${mockException.message}", mockException)
                            // If mock data fails too, try to load from database
                            loadLatestFromDatabase()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error getting latest manga: ${e.message}", e)
                    // Try to load from database
                    loadLatestFromDatabase()
                }
            } else {
                Log.d(TAG, "No network connection, loading latest manga from database")
                loadLatestFromDatabase()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest manga: ${e.message}", e)
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun loadLatestFromDatabase(): Result<List<Manga>> {
        try {
            var latestManga = emptyList<Manga>()
            mangaDao.getMangasByPage(-1).collect { entities ->
                latestManga = entities.map { it.toManga() }
                Log.d(TAG, "Loaded ${entities.size} latest manga from database")
            }

            return if (latestManga.isNotEmpty()) {
                Result.Success(latestManga)
            } else {
                Result.Error("No internet connection and no cached latest manga available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading latest manga from database: ${e.message}")
            return Result.Error("Failed to load latest manga. Pull to refresh.")
        }
    }

    override suspend fun getMangaById(id: Int): Result<Manga> {
        return try {
            // Check database first
            val localManga = mangaDao.getMangaById(id)
            if (localManga != null) {
                return Result.Success(localManga.toManga())
            }

            try {
                // Fetch from API if not in database
                val mangaDto = mangaApiService.getMangaById(id)
                val manga = mangaDto.toManga()

                // Save to database
                mangaDao.insertMangas(listOf(MangaEntity.fromManga(manga, 1))) // Use page 1 as default

                Result.Success(manga)
            } catch (e: IOException) {
                Result.Error("No internet connection and manga not found in cache")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting manga by id: ${e.message}")
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun searchManga(query: String): Result<List<Manga>> {
        return try {
            try {
                // Try to search from API first
                val searchResults = mangaApiService.searchManga(query)
                val mangaList = searchResults.data.map { it.toManga() }
                Result.Success(mangaList)
            } catch (e: IOException) {
                // Network error, try to search in database
                Log.d(TAG, "Network error, searching in database: ${e.message}")
                val localResults = mangaDao.searchMangas(query).map { it.toManga() }
                if (localResults.isNotEmpty()) {
                    Result.Success(localResults)
                } else {
                    Result.Error("No internet connection and no matching results in cache")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching manga: ${e.message}")
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getChapters(mangaId: Int): Result<List<ChapterDto>> {
        return try {
            val response = mangaApiService.getChapters(mangaId)
            Result.Success(response.chapters)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapters: ${e.message}")
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getChapterImages(mangaId: Int, chapterNumber: String): Result<List<String>> {
        return try {
            val response = mangaApiService.getChapterImages(mangaId, chapterNumber)
            Result.Success(response.images)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter images: ${e.message}")
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override fun getLocalMangaList(): Flow<List<Manga>> {
        return mangaDao.getAllMangas().map { entities ->
            entities.map { it.toManga() }
        }
    }

    /**
     * Creates mock manga data for testing and fallback purposes
     */
    private fun createMockMangaData(page: Int, pageSize: Int): List<Manga> {
        Log.d(TAG, "Creating mock manga data for page $page with size $pageSize")
        val startId = (page - 1) * pageSize + 1

        return List(pageSize) { index ->
            val id = startId + index
            Manga(
                id = id,
                title = "Manga Title $id",
                description = "This is a description for manga $id. This is mock data created as a fallback when the API is not available.",
                coverImage = "https://via.placeholder.com/300x450.png?text=Manga+$id",
                author = "Author $id",
                genres = listOf("Action", "Adventure", "Fantasy"),
                chapters = (5..50).random(),
                status = if (id % 3 == 0) "Completed" else "Ongoing",
                rating = 4.5f
            )
        }.also {
            Log.d(TAG, "Created ${it.size} mock manga items")
        }
    }

    /**
     * Creates mock latest manga data for testing and fallback purposes
     */
    private fun createMockLatestManga(): List<Manga> {
        Log.d(TAG, "Creating mock latest manga data")
        val popularTitles = listOf(
            "Naruto",
            "One Piece",
            "Attack on Titan",
            "My Hero Academia",
            "Demon Slayer",
            "Jujutsu Kaisen",
            "Dragon Ball",
            "Tokyo Ghoul",
            "Bleach",
            "Death Note"
        )

        return List(10) { index ->
            val id = 1000 + index
            Manga(
                id = id,
                title = popularTitles[index],
                description = "This is a popular manga series. This is mock data created as a fallback when the API is not available.",
                coverImage = "https://via.placeholder.com/300x450.png?text=${popularTitles[index].replace(" ", "+")}",
                author = "Famous Author $index",
                genres = listOf("Action", "Adventure", "Fantasy"),
                chapters = (100..300).random(),
                status = if (index % 3 == 0) "Completed" else "Ongoing",
                rating = 4.8f
            )
        }.also {
            Log.d(TAG, "Created ${it.size} mock latest manga items")
        }
    }
}
