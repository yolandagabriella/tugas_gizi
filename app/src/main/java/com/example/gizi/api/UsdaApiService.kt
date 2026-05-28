package com.example.gizi.api

import com.example.gizi.model.FoodSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApiService {

    @GET("foods/search")
    suspend fun searchFood(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("pageSize") pageSize: Int = 10
    ): FoodSearchResponse
}