package com.example.gizi.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gizi.IndonesianFoodDatabase
import com.example.gizi.api.RetrofitClient
import com.example.gizi.helper.PrefsHelper
import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NutrisiViewModel : ViewModel() {

    private val apiKey = "uoHcRktqABRUv6t6qqVoETSyiot2vKHRdVsOMOh8"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResults = MutableLiveData<List<FoodItem>>()
    val searchResults: LiveData<List<FoodItem>> = _searchResults

    private val _selectedFoods = MutableLiveData<MutableList<FoodItem>>(mutableListOf())
    val selectedFoods: LiveData<MutableList<FoodItem>> = _selectedFoods

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val totalKalori: Double get() = _selectedFoods.value?.sumOf { it.getKalori() } ?: 0.0
    val totalProtein: Double get() = _selectedFoods.value?.sumOf { it.getProtein() } ?: 0.0
    val totalKarbo: Double get() = _selectedFoods.value?.sumOf { it.getKarbohidrat() } ?: 0.0
    val totalLemak: Double get() = _selectedFoods.value?.sumOf { it.getLemak() } ?: 0.0

    // ==================== SEARCH ====================

    fun searchFood(query: String) {
        if (query.isBlank()) return
        val localResults = IndonesianFoodDatabase.search(query)
        if (localResults.isNotEmpty()) {
            _searchResults.value = localResults
        } else {
            searchFromUSDA(query)
        }
    }

    private fun searchFromUSDA(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.instance.searchFood(query, apiKey)
                val translated = response.foods.map { food ->
                    food.copy(description = translateFoodName(food.description))
                }
                _searchResults.value = translated
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mencari makanan: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun translateFoodName(englishName: String): String {
        val name = englishName.lowercase()
        return when {
            name.contains("rice") && name.contains("white") -> "Nasi Putih"
            name.contains("chicken") && name.contains("fried") -> "Ayam Goreng"
            name.contains("egg") && name.contains("boiled") -> "Telur Rebus"
            else -> englishName
        }
    }

    // ==================== SELECTED FOODS ====================

    fun addFood(food: FoodItem) {
        val currentList = _selectedFoods.value ?: mutableListOf()
        if (currentList.none { it.fdcId == food.fdcId }) {
            currentList.add(food)
            _selectedFoods.value = currentList
        }
    }

    fun removeFood(food: FoodItem) {
        val currentList = _selectedFoods.value ?: mutableListOf()
        currentList.remove(food)
        _selectedFoods.value = currentList
    }

    // ==================== SIMPAN KE LOKAL + FIRESTORE ====================

    fun simpanKaloriHarian(context: Context) {
        val uid = auth.currentUser?.uid
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val foods = _selectedFoods.value ?: emptyList()

        // 1. Simpan lokal dulu (cepat, offline-friendly)
        PrefsHelper.simpanKalori(
            context,
            totalKalori.toFloat(),
            totalProtein.toFloat(),
            totalKarbo.toFloat(),
            totalLemak.toFloat(),
            foods
        )

        Toast.makeText(context, "Data tersimpan!", Toast.LENGTH_SHORT).show()

        // 2. Simpan ke Firestore (kalau login)
        if (uid == null) {
            Log.w("FIRESTORE", "User belum login, skip cloud sync")
            return
        }

        // Konversi list FoodItem ke format Map yang bisa disimpan Firestore
        val makananList = foods.map { food ->
            hashMapOf(
                "fdcId" to food.fdcId,
                "description" to food.description,
                "kalori" to food.getKalori(),
                "protein" to food.getProtein(),
                "karbo" to food.getKarbohidrat(),
                "lemak" to food.getLemak()
            )
        }

        val nutritionData = hashMapOf(
            "totalKalori" to totalKalori,
            "totalProtein" to totalProtein,
            "totalKarbo" to totalKarbo,
            "totalLemak" to totalLemak,
            "tanggal" to today,
            "lastUpdate" to Timestamp.now(),
            "makanan" to makananList
        )

        db.collection("users").document(uid)
            .collection("nutrition_logs").document(today)
            .set(nutritionData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Berhasil sync ke cloud: $today")
                Toast.makeText(context, "Cloud tersinkron!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Gagal sync: ${e.message}")
            }
    }

    // ==================== LOAD DARI FIRESTORE ====================

    fun loadFromFirestore(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        db.collection("users").document(uid)
            .collection("nutrition_logs").document(today)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) return@addOnSuccessListener

                // Parse makanan dari Firestore
                @Suppress("UNCHECKED_CAST")
                val makananRaw = document.get("makanan") as? List<Map<String, Any>> ?: return@addOnSuccessListener

                val foods = makananRaw.map { map ->
                    FoodItem(
                        fdcId = (map["fdcId"] as? Long)?.toInt() ?: 0,
                        description = map["description"] as? String ?: "",
                        foodNutrients = listOf(
                            FoodNutrient("Energy", (map["kalori"] as? Double) ?: 0.0, "kcal"),
                            FoodNutrient("Protein", (map["protein"] as? Double) ?: 0.0, "g"),
                            FoodNutrient("Carbohydrate", (map["karbo"] as? Double) ?: 0.0, "g"),
                            FoodNutrient("Total lipid", (map["lemak"] as? Double) ?: 0.0, "g")
                        )
                    )
                }

                // Update ViewModel
                val currentList = _selectedFoods.value ?: mutableListOf()
                foods.forEach { food ->
                    if (currentList.none { it.fdcId == food.fdcId }) {
                        currentList.add(food)
                    }
                }
                _selectedFoods.value = currentList

                // Sync ke lokal juga
                PrefsHelper.simpanKalori(
                    context,
                    (document.getDouble("totalKalori") ?: 0.0).toFloat(),
                    (document.getDouble("totalProtein") ?: 0.0).toFloat(),
                    (document.getDouble("totalKarbo") ?: 0.0).toFloat(),
                    (document.getDouble("totalLemak") ?: 0.0).toFloat(),
                    foods
                )

                Log.d("FIRESTORE", "Berhasil load ${foods.size} makanan dari cloud")
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Gagal load dari cloud: ${e.message}")
            }
    }
}
