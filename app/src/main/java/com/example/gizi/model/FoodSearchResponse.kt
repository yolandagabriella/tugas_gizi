package com.example.gizi.model

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    @SerializedName("foods") val foods: List<FoodItem>
)

data class FoodItem(
    @SerializedName("fdcId") val fdcId: Int,
    @SerializedName("description") val description: String,
    @SerializedName("foodNutrients") val foodNutrients: List<FoodNutrient>
) {
    // Helper untuk ambil nutrisi spesifik
    fun getKalori(): Double =
        foodNutrients.find { it.nutrientName.contains("Energy", ignoreCase = true) }
            ?.value ?: 0.0

    fun getProtein(): Double =
        foodNutrients.find { it.nutrientName.contains("Protein", ignoreCase = true) }
            ?.value ?: 0.0

    fun getKarbohidrat(): Double =
        foodNutrients.find {
            it.nutrientName.contains("Carbohydrate", ignoreCase = true)
        }?.value ?: 0.0

    fun getLemak(): Double =
        foodNutrients.find {
            it.nutrientName.contains("Total lipid", ignoreCase = true)
        }?.value ?: 0.0
}
