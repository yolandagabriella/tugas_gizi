package com.example.gizi.model

data class Video(
    val id: String,
    val title: String,
    val level: String,
    val category: String,
    val youtubeUrl: String,
    val thumbnailUrl: String? = null
)
