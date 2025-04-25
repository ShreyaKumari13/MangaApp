package com.example.androiddevelopmenttask.data.api.model

import com.google.gson.annotations.SerializedName

data class MangaListResponse(
    @SerializedName("data")
    val data: List<MangaDto> = emptyList(),

    @SerializedName("code")
    val code: Int = 200,

    @SerializedName("page")
    var page: Int = 1,

    @SerializedName("totalItems")
    var totalItems: Int = 0,

    @SerializedName("totalPages")
    var totalPages: Int = 0,

    @SerializedName("status")
    val status: String = if (code == 200) "success" else "error",

    @SerializedName("message")
    val message: String = ""
) {
    fun withPagination(currentPage: Int, pageSize: Int): MangaListResponse {
        return this.copy(
            page = currentPage,
            totalItems = data.size,
            totalPages = if (data.size < pageSize) currentPage else currentPage + 1
        )
    }
}
