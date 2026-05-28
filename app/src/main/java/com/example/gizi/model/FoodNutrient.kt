package com.example.gizi.model

import com.google.gson.annotations.SerializedName

data class FoodNutrient(
    @SerializedName("nutrientName") val nutrientName: String,
    @SerializedName("value") val value: Double,
    @SerializedName("unitName") val unitName: String,
)
