package com.example.androiddevelopmenttask.data.api

import com.example.androiddevelopmenttask.data.api.model.ChapterListResponse
import com.example.androiddevelopmenttask.data.api.model.ChapterImagesResponse
import com.example.androiddevelopmenttask.data.api.model.MangaDto
import com.example.androiddevelopmenttask.data.api.model.MangaListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaApiService {
    /**
     * Get a list of manga with pagination
     * Endpoint: /fetch-manga
     */
    @GET("fetch-manga")
    suspend fun getMangaList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): MangaListResponse

    /**
     * Get latest manga
     * Endpoint: /fetch-latest
     */
    @GET("fetch-latest")
    suspend fun getLatestManga(): MangaListResponse

    /**
     * Search for manga by title, author, or genre
     * Endpoint: /search-manga
     */
    @GET("search-manga")
    suspend fun searchManga(@Query("query") query: String): MangaListResponse

    /**
     * Get manga details by ID
     * Endpoint: /get-manga
     */
    @GET("get-manga")
    suspend fun getMangaById(@Query("id") id: Int): MangaDto

    /**
     * Get chapters for a manga
     * Endpoint: /fetch-chapters
     */
    @GET("fetch-chapters")
    suspend fun getChapters(@Query("id") mangaId: Int): ChapterListResponse

    /**
     * Get images for a chapter
     * Endpoint: /fetch-images
     */
    @GET("fetch-images")
    suspend fun getChapterImages(
        @Query("id") mangaId: Int,
        @Query("chapter") chapterNumber: String
    ): ChapterImagesResponse
}
