package com.example.androiddevelopmenttask.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.androiddevelopmenttask.data.db.entity.MangaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<MangaEntity>)

    @Query("SELECT * FROM mangas WHERE id = :id LIMIT 1")
    suspend fun getMangaById(id: Int): MangaEntity?

    @Query("SELECT * FROM mangas ORDER BY id ASC")
    fun getAllMangas(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM mangas WHERE page = :page ORDER BY id ASC")
    fun getMangasByPage(page: Int): Flow<List<MangaEntity>>

    @Query("SELECT * FROM mangas WHERE title LIKE '%' || :query || '%'")
    suspend fun searchMangas(query: String): List<MangaEntity>

    @Query("DELETE FROM mangas WHERE page = :page")
    suspend fun deleteMangasByPage(page: Int)

    @Query("SELECT MAX(page) FROM mangas")
    suspend fun getMaxPage(): Int?

    @Transaction
    suspend fun clearAndInsertForPage(page: Int, mangas: List<MangaEntity>) {
        deleteMangasByPage(page)
        insertMangas(mangas)
    }
}
