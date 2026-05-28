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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class NutrisiViewModel : ViewModel() {

    private val apiKey = "uoHcRktqABRUv6t6qqVoETSyiot2vKHRdVsOMOh8"
    private val db = FirebaseFirestore.getInstance() // Ini otomatis menembak database (default)
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

    fun simpanKaloriHarian(context: Context) {
        val user = auth.currentUser
        val userId = user?.uid

        Log.d("FIRESTORE_DEBUG", "Memulai proses simpan. UserID: $userId")
        
        if (userId == null) {
            Log.e("FIRESTORE_DEBUG", "UserID NULL! User belum login ke Firebase.")
            Toast.makeText(context, "❌ Error: Kamu belum login ke Cloud", Toast.LENGTH_LONG).show()
            return
        }

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val nutritionData = hashMapOf(
            "totalKalori" to totalKalori,
            "totalProtein" to totalProtein,
            "totalKarbo" to totalKarbo,
            "totalLemak" to totalLemak,
            "tanggal" to today,
            "lastUpdate" to com.google.firebase.Timestamp.now(),
            "makanan" to (_selectedFoods.value?.map { it.description } ?: emptyList()),
        )

        Log.d("FIRESTORE_DEBUG", "Data yang akan dikirim: $nutritionData")

        // Agar dokumen User tidak 'abu-abu', kita update dulu dokumen induknya
        db.collection("users").document(userId)
            .update("lastSync", com.google.firebase.Timestamp.now())
            .addOnFailureListener { 
                // Jika gagal update (karena dokumen belum ada), kita create
                db.collection("users").document(userId).set(hashMapOf("email" to user.email))
            }

        // SIMPAN KE SUB-COLLECTION HISTORY
        db.collection("users").document(userId)
            .collection("history").document(today)
            .set(nutritionData)
            .addOnSuccessListener {
                Log.d("FIRESTORE_DEBUG", "✅ BERHASIL! Data muncul di Firestore untuk tanggal: $today")
                Toast.makeText(context, "✅ Cloud Berhasil Sinkron!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_DEBUG", "❌ GAGAL TOTAL: ${e.message}")
                e.printStackTrace()
                Toast.makeText(context, "❌ Gagal Cloud: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }

        // Simpan Lokal tetap jalan
        PrefsHelper.simpanKalori(context, totalKalori.toFloat(), totalProtein.toFloat(), totalKarbo.toFloat(), totalLemak.toFloat(), _selectedFoods.value ?: emptyList())
    }
}
