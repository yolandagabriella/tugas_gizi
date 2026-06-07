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

    private val _statusMessage = MutableLiveData<Pair<String, Boolean>?>() // message to isAchievement
    val statusMessage: LiveData<Pair<String, Boolean>?> = _statusMessage

    private val removedFoodIds = mutableSetOf<Int>()

    var targetKalori: Int = 2000
    private var savedKaloriSaatIni: Float = 0f

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

    fun initTarget(context: Context) {
        targetKalori = PrefsHelper.getTargetKalori(context)
        savedKaloriSaatIni = PrefsHelper.getKaloriHariIni(context)
    }

    fun addFood(food: FoodItem) {
        // Cek kalau makanan ini pernah di-X, skip!
        if (removedFoodIds.contains(food.fdcId)) return

        val currentList = _selectedFoods.value ?: mutableListOf()
        val oldTotal = savedKaloriSaatIni + totalKalori
        // Tambah timestamp biar fdcId unik walau makanan sama
        val foodBaru = food.copy(fdcId = System.currentTimeMillis().toInt())
        currentList.add(foodBaru)
        _selectedFoods.value = currentList
        checkCalorieStatus(oldTotal, savedKaloriSaatIni + totalKalori)
    }

    private fun checkCalorieStatus(oldTotal: Double, newTotal: Double) {
        val target = targetKalori.toDouble()

        when {
            // Tepat mencapai target
            newTotal >= target && oldTotal < target -> {
                _statusMessage.value = Pair(
                    "🎉 Selamat! Target kalori harianmu sudah tercapai!\nMau reset untuk memulai hari baru?",
                    true
                )
            }
            // Melebihi target
            newTotal > target -> {
                val lebih = (newTotal - target).toInt()
                _statusMessage.value = Pair(
                    "⚠️ Kamu sudah melebihi target kalori sebanyak $lebih kkal hari ini!",
                    false
                )
            }
            // Mendekati target (90%)
            newTotal >= target * 0.9 && oldTotal < target * 0.9 -> {
                _statusMessage.value = Pair(
                    "🔔 Hampir mencapai target kalori harianmu!",
                    false
                )
            }
        }
    }

    fun resetMakanan(context: Context) {
        val uid = auth.currentUser?.uid
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        _selectedFoods.value = mutableListOf()
        removedFoodIds.clear() // Reset juga daftar yang dihapus
        PrefsHelper.clearData(context)
        _statusMessage.value = null

        // Reset di Firestore juga
        uid?.let {
            db.collection("users").document(it)
                .collection("nutrition_logs").document(today)
                .delete()
        }
    }

    fun resetStatusMessage() {
        _statusMessage.value = null
    }

    fun removeFood(food: FoodItem) {
        // Simpan id makanan yang dihapus
        removedFoodIds.add(food.fdcId)

        val currentList = _selectedFoods.value ?: mutableListOf()
        currentList.remove(food)
        _selectedFoods.value = currentList
    }

    // ==================== SIMPAN KE LOKAL + FIRESTORE ====================

    fun simpanKaloriHarian(context: Context) {
        val uid = auth.currentUser?.uid
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // 1. Ambil data lama
        val oldKalori = PrefsHelper.getKaloriHariIni(context)
        val oldProtein = PrefsHelper.getProteinHariIni(context)
        val oldKarbo = PrefsHelper.getKarboHariIni(context)
        val oldLemak = PrefsHelper.getLemakHariIni(context)
        val oldFoods = PrefsHelper.getFoodHistory(context).toMutableList()

        // 2. Gabungkan dengan data baru
        val currentSessionFoods = _selectedFoods.value ?: emptyList()
        val newTotalKalori = oldKalori + totalKalori.toFloat()
        val newTotalProtein = oldProtein + totalProtein.toFloat()
        val newTotalKarbo = oldKarbo + totalKarbo.toFloat()
        val newTotalLemak = oldLemak + totalLemak.toFloat()
        oldFoods.addAll(currentSessionFoods)

        // 3. Simpan akumulasi
        PrefsHelper.simpanKalori(
            context,
            newTotalKalori,
            newTotalProtein,
            newTotalKarbo,
            newTotalLemak,
            oldFoods
        )

        // Reset list di UI biar ga double
        _selectedFoods.value = mutableListOf()
        savedKaloriSaatIni = newTotalKalori

        if (uid == null) return

        // Konversi list FoodItem ke format Map
        val makananList = oldFoods.map { food ->
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
            "totalKalori" to newTotalKalori,
            "totalProtein" to newTotalProtein,
            "totalKarbo" to newTotalKarbo,
            "totalLemak" to newTotalLemak,
            "tanggal" to today,
            "lastUpdate" to Timestamp.now(),
            "makanan" to makananList
        )

        db.collection("users").document(uid)
            .collection("nutrition_logs").document(today)
            .set(nutritionData)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Berhasil sync ke cloud: $today")
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
