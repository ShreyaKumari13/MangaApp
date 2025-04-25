package com.example.androiddevelopmenttask.data.api.model

import com.google.gson.annotations.SerializedName

data class ChapterListResponse(
    @SerializedName("chapters")
    val chapters: List<ChapterDto> = emptyList(),
    
    @SerializedName("status")
    val status: String = "",
    
    @SerializedName("message")
    val message: String = ""
)

data class ChapterDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("chapter_number")
    val chapterNumber: String = "",
    
    @SerializedName("release_date")
    val releaseDate: String = ""
)
