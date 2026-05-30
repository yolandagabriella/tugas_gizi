package com.example.gizi.helper

import android.content.Context
import com.example.gizi.model.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrefsHelper {

    private const val PREFS_NAME = "GIZI_PREFS"
    private val gson = Gson()

    // ==================== PROFIL ====================

    fun simpanProfil(
        context: Context,
        beratBadan: Float,
        tinggiBadan: Float,
        tujuan: String,
        fotoUri: String = ""
    ) {
        val targetKalori = hitungTargetKalori(beratBadan, tinggiBadan, tujuan)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat("berat_badan", beratBadan)
            .putFloat("tinggi_badan", tinggiBadan)
            .putString("tujuan_kesehatan", tujuan)
            .putInt("target_kalori", targetKalori)
            .putString("foto_profil_uri", fotoUri)
            .apply()
    }

    fun hitungTargetKalori(beratBadan: Float, tinggiBadan: Float, tujuan: String): Int {
        // Rumus Harris-Benedict (asumsi aktivitas sedang, gender netral)
        val bmr = (10 * beratBadan) + (6.25f * tinggiBadan) - 161
        val tdee = bmr * 1.55f // aktivitas sedang
        return when (tujuan) {
            "Turun Berat Badan" -> (tdee - 500).toInt().coerceAtLeast(1200)
            "Naik Berat Badan"  -> (tdee + 500).toInt()
            else                -> tdee.toInt() // Jaga Berat Badan
        }
    }

    fun getBeratBadan(context: Context): Float =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat("berat_badan", 0f)

    fun getTinggiBadan(context: Context): Float =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat("tinggi_badan", 0f)

    fun getTujuan(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("tujuan_kesehatan", "Jaga Berat Badan") ?: "Jaga Berat Badan"

    fun getFotoProfil(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("foto_profil_uri", "") ?: ""

    fun isProfilLengkap(context: Context): Boolean =
        getBeratBadan(context) > 0f && getTinggiBadan(context) > 0f

    fun isOnboardingDone(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("onboarding_done", false)

    fun setOnboardingDone(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("onboarding_done", true)
            .apply()
    }

    // ==================== KALORI HARIAN ====================

    fun simpanKalori(
        context: Context,
        kalori: Float,
        protein: Float,
        karbo: Float,
        lemak: Float,
        foods: List<FoodItem> = emptyList()
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val foodsJson = gson.toJson(foods)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat("kalori_hari_ini", kalori)
            .putFloat("protein_hari_ini", protein)
            .putFloat("karbo_hari_ini", karbo)
            .putFloat("lemak_hari_ini", lemak)
            .putString("foods_history", foodsJson)
            .putString("tanggal_simpan", today)
            .apply()
    }

    fun getKaloriHariIni(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (isToday(prefs)) prefs.getFloat("kalori_hari_ini", 0f) else 0f
    }

    fun getFoodHistory(context: Context): List<FoodItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!isToday(prefs)) return emptyList()
        val json = prefs.getString("foods_history", null) ?: return emptyList()
        val type = object : TypeToken<List<FoodItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getProteinHariIni(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (isToday(prefs)) prefs.getFloat("protein_hari_ini", 0f) else 0f
    }

    fun getKarboHariIni(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (isToday(prefs)) prefs.getFloat("karbo_hari_ini", 0f) else 0f
    }

    fun getLemakHariIni(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (isToday(prefs)) prefs.getFloat("lemak_hari_ini", 0f) else 0f
    }

    private fun isToday(prefs: android.content.SharedPreferences): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return prefs.getString("tanggal_simpan", "") == today
    }

    fun getTargetKalori(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("target_kalori", 2000)

    fun simpanTargetKalori(context: Context, target: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt("target_kalori", target)
            .apply()
    }

    fun clearData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove("kalori_hari_ini")
            .remove("protein_hari_ini")
            .remove("karbo_hari_ini")
            .remove("lemak_hari_ini")
            .remove("foods_history")
            .remove("tanggal_simpan")
            .apply()
    }

    fun clearAllData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
