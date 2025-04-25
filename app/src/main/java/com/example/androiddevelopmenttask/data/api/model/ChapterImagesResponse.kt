package com.example.androiddevelopmenttask.data.api.model

import com.google.gson.annotations.SerializedName

data class ChapterImagesResponse(
    @SerializedName("images")
    val images: List<String> = emptyList(),
    
    @SerializedName("status")
    val status: String = "",
    
    @SerializedName("message")
    val message: String = ""
)
