package com.example.androiddevelopmenttask.data.api.model

import com.example.androiddevelopmenttask.domain.model.Manga
import com.google.gson.annotations.SerializedName

data class MangaDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String = "", // Some manga might not have descriptions

    @SerializedName("cover_image")
    val coverImage: String = "", // Default empty string if no image

    @SerializedName("author")
    val author: String = "Unknown", // Default author if not provided

    @SerializedName("genres")
    val genres: List<String> = emptyList(),

    @SerializedName("chapters")
    val chapters: Int = 0,

    @SerializedName("status")
    val status: String = "",

    @SerializedName("rating")
    val rating: Float = 0f,

    @SerializedName("published")
    val published: String = "",

    @SerializedName("popularity")
    val popularity: Int = 0
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
}
