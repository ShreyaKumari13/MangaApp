package com.example.androiddevelopmenttask.domain.model

data class Manga(
    val id: Int,
    val title: String,
    val description: String,
    val coverImage: String,
    val author: String,
    val genres: List<String> = emptyList(),
    val chapters: Int = 0,
    val status: String = "",
    val rating: Float = 0f
)
