package com.example.androiddevelopmenttask.data.api.model

import com.google.gson.annotations.SerializedName

data class MangaListResponse(
    @SerializedName("manga")
    val data: List<MangaDto> = emptyList(),

    @SerializedName("current_page")
    val page: Int = 1,

    @SerializedName("total_pages")
    val totalPages: Int = 1,

    @SerializedName("total_items")
    val totalItems: Int = 0,

    @SerializedName("status")
    val status: String = "",

    @SerializedName("message")
    val message: String = ""
)
