package com.example.gizi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gizi.api.YouTubeClient
import com.example.gizi.model.Video
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val apiKey = "AIzaSyDC7MEAN-5GonpHwLPVpvjxOO4VE2W_jpQ"
    
    var selectedLevel: String = "Pemula"
    var selectedKategori: String = "Yoga"

    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val levelEng = when (selectedLevel) {
                    "Pemula" -> "Beginner"
                    "Menengah" -> "Intermediate"
                    "Mahir" -> "Advanced"
                    else -> selectedLevel
                }
                val query = "$selectedKategori workout for $levelEng"
                
                val response = YouTubeClient.instance.searchVideos(
                    query = query,
                    maxResults = 15,
                    apiKey = apiKey
                )
                
                val videoList = response.items.map { item ->
                    Video(
                        id = item.id.videoId,
                        title = item.snippet.title,
                        level = selectedLevel,
                        category = selectedKategori,
                        youtubeUrl = "https://www.youtube.com/watch?v=${item.id.videoId}",
                        thumbnailUrl = item.snippet.thumbnails.medium.url
                    )
                }
                _videos.value = videoList
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat video: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
