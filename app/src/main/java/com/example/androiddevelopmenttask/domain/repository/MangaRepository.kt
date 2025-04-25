package com.example.androiddevelopmenttask.domain.repository

import com.example.androiddevelopmenttask.data.api.model.ChapterDto
import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    suspend fun getMangaList(page: Int, pageSize: Int): Result<List<Manga>>
    suspend fun getLatestManga(): Result<List<Manga>>
    suspend fun getMangaById(id: Int): Result<Manga>
    suspend fun searchManga(query: String): Result<List<Manga>>
    suspend fun getChapters(mangaId: Int): Result<List<ChapterDto>>
    suspend fun getChapterImages(mangaId: Int, chapterNumber: String): Result<List<String>>
    fun getLocalMangaList(): Flow<List<Manga>>
}
