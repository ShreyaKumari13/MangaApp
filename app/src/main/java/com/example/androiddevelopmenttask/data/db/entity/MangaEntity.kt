package com.example.androiddevelopmenttask.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.androiddevelopmenttask.data.db.converter.StringListConverter
import com.example.androiddevelopmenttask.domain.model.Manga

@Entity(tableName = "mangas")
@TypeConverters(StringListConverter::class)
data class MangaEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val coverImage: String,
    val author: String,
    val genres: List<String>,
    val chapters: Int,
    val status: String,
    val rating: Float,
    val page: Int // To track which page this manga belongs to for pagination
) {
    fun toManga(): Manga {
        return Manga(
            id = id,
            title = title,
            description = description,
            coverImage = coverImage,
            author = author,
            genres = genres,
            chapters = chapters,
            status = status,
            rating = rating
        )
    }
    
    companion object {
        fun fromManga(manga: Manga, page: Int): MangaEntity {
            return MangaEntity(
                id = manga.id,
                title = manga.title,
                description = manga.description,
                coverImage = manga.coverImage,
                author = manga.author,
                genres = manga.genres,
                chapters = manga.chapters,
                status = manga.status,
                rating = manga.rating,
                page = page
            )
        }
    }
}
