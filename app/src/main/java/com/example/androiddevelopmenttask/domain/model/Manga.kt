package com.example.androiddevelopmenttask.domain.model

data class Manga(
    val id: Int,
    val title: String,
    val subTitle: String = "",
    val description: String,
    val coverImage: String,
    val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val chapters: Int = 0,
    val status: String = "",
    val nsfw: Boolean = false,
    val type: String = "japan",
    val createdAt: String = "",
    val updatedAt: String = ""
)
