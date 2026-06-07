package com.example.gizi.model

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: VideoId,
    val snippet: VideoSnippet
)

data class VideoId(
    val videoId: String
)

data class VideoSnippet(
    val title: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    @SerializedName("medium") val medium: ThumbnailDetails
)

data class ThumbnailDetails(
    val url: String
)
