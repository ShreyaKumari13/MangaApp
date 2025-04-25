package com.example.androiddevelopmenttask.data.api.model

import com.example.androiddevelopmenttask.domain.model.Manga
import com.google.gson.annotations.SerializedName

data class MangaDto(
    @SerializedName("id")
    val idString: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("sub_title")
    val subTitle: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("thumb")
    val coverImage: String = "",

    @SerializedName("summary")
    val description: String = "",

    @SerializedName("authors")
    val authors: List<String> = emptyList(),

    @SerializedName("genres")
    val genres: List<String> = emptyList(),

    @SerializedName("nsfw")
    val nsfw: Boolean = false,

    @SerializedName("type")
    val type: String = "",

    @SerializedName("total_chapter")
    val chapters: Int = 0,

    @SerializedName("create_at")
    val createdAt: Long = 0,

    @SerializedName("update_at")
    val updatedAt: Long = 0
) {
    // Generate a numeric ID from the string ID for database compatibility
    val id: Int
        get() = idString.hashCode()

    fun toManga(): Manga {
        return Manga(
            id = id,
            title = title.trim(),
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
}
