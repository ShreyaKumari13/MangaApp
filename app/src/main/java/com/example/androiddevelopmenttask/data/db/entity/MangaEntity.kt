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
    val subTitle: String,
    val description: String,
    val coverImage: String,
    val authors: List<String>,
    val genres: List<String>,
    val chapters: Int,
    val status: String,
    val nsfw: Boolean,
    val type: String,
    val createdAt: Long,
    val updatedAt: Long,
    val page: Int // To track which page this manga belongs to for pagination
) {
    fun toManga(): Manga {
        return Manga(
            id = id,
            title = title,
            subTitle = subTitle,
            description = description,
            coverImage = coverImage,
            authors = authors,
            genres = genres,
            chapters = chapters,
            status = status,
            nsfw = nsfw,
            type = type,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString()
        )
    }

    companion object {
        fun fromManga(manga: Manga, page: Int): MangaEntity {
            return MangaEntity(
                id = manga.id,
                title = manga.title,
                subTitle = manga.subTitle,
                description = manga.description,
                coverImage = manga.coverImage,
                authors = manga.authors,
                genres = manga.genres,
                chapters = manga.chapters,
                status = manga.status,
                nsfw = manga.nsfw,
                type = manga.type,
                createdAt = manga.createdAt.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = manga.updatedAt.toLongOrNull() ?: System.currentTimeMillis(),
                page = page
            )
        }
    }
}
