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
     * Endpoint: /manga/fetch (as confirmed by Postman test)
     */
    @GET("manga/fetch")
    suspend fun getMangaList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): MangaListResponse


    /**
     * Get latest manga
     * Endpoint: /manga/fetch-latest
     */
    @GET("manga/fetch-latest")
    suspend fun getLatestManga(): MangaListResponse

    /**
     * Search for manga by title, author, or genre
     * Endpoint: /manga/search
     */
    @GET("manga/search")
    suspend fun searchManga(@Query("q") query: String): MangaListResponse


    /**
     * Get manga details by ID
     * Endpoint: /manga/details
     */
    @GET("manga/details")
    suspend fun getMangaById(@Query("id") id: Int): MangaDto



    /**
     * Get chapters for a manga
     * Endpoint: /manga/chapters
     */
    @GET("manga/chapters")
    suspend fun getChapters(@Query("id") mangaId: Int): ChapterListResponse



    /**
     * Get images for a chapter
     * Endpoint: /chapter/images
     */
    @GET("chapter/images")
    suspend fun getChapterImages(
        @Query("id") mangaId: Int,
        @Query("chapter") chapterNumber: String
    ): ChapterImagesResponse
}
