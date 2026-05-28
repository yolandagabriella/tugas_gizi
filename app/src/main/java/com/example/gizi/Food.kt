package com.example.gizi

data class Food(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    var isSelected: Boolean = false,
)