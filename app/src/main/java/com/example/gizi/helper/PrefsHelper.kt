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

    fun hitungTargetKaloriDariTargetBerat(
        beratSekarang: Float,
        targetBerat: Float,
        tinggiBadan: Float,
        usia: Int = 25,
        jenisKelamin: String = "Perempuan"
    ): Int {
        val bmr = if (jenisKelamin == "Laki-laki") {
            (10 * beratSekarang) + (6.25f * tinggiBadan) - (5 * usia) + 5
        } else {
            (10 * beratSekarang) + (6.25f * tinggiBadan) - (5 * usia) - 161
        }
        val tdee = bmr * 1.55f

        return when {
            beratSekarang > targetBerat + 1f -> (tdee - 500).toInt().coerceAtLeast(1200) // turun
            beratSekarang < targetBerat - 1f -> (tdee + 500).toInt()                     // naik
            else                             -> tdee.toInt()                              // jaga
        }
    }

    fun hitungTargetKalori(
        beratBadan: Float,
        tinggiBadan: Float,
        tujuan: String,
        usia: Int = 25,
        jenisKelamin: String = "Perempuan"
    ): Int {
        val bmr = if (jenisKelamin == "Laki-laki") {
            (10 * beratBadan) + (6.25f * tinggiBadan) - (5 * usia) + 5
        } else {
            (10 * beratBadan) + (6.25f * tinggiBadan) - (5 * usia) - 161
        }
        val tdee = bmr * 1.55f
        return when (tujuan) {
            "Turun Berat Badan" -> (tdee - 500).toInt().coerceAtLeast(1200)
            "Naik Berat Badan"  -> (tdee + 500).toInt()
            else                -> tdee.toInt()
        }
    }

    fun getTargetBerat(context: Context): Float =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat("target_berat", 0f)

    fun simpanTargetBerat(context: Context, targetBerat: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat("target_berat", targetBerat)
            .apply()
    }

    fun simpanProfil(
        context: Context,
        beratBadan: Float,
        tinggiBadan: Float,
        tujuan: String,
        fotoUri: String = "",
        usia: Int = 25,
        jenisKelamin: String = "Perempuan",
        targetBerat: Float = 0f
    ) {
        val targetKalori = if (targetBerat > 0f) {
            hitungTargetKaloriDariTargetBerat(beratBadan, targetBerat, tinggiBadan, usia, jenisKelamin)
        } else {
            hitungTargetKalori(beratBadan, tinggiBadan, tujuan, usia, jenisKelamin)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat("berat_badan", beratBadan)
            .putFloat("tinggi_badan", tinggiBadan)
            .putString("tujuan_kesehatan", tujuan)
            .putInt("target_kalori", targetKalori)
            .putString("foto_profil_uri", fotoUri)
            .putInt("usia", usia)
            .putString("jenis_kelamin", jenisKelamin)
            .putFloat("target_berat", targetBerat)
            .apply()
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

    fun getUsia(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("usia", 25)

    fun getJenisKelamin(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("jenis_kelamin", "Perempuan") ?: "Perempuan"

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

    fun isTargetPopupShown(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("target_popup_shown", false)

    fun setTargetPopupShown(context: Context, shown: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("target_popup_shown", shown)
            .apply()
    }

    fun isOverPopupShown(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("over_popup_shown", false)

    fun setOverPopupShown(context: Context, shown: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("over_popup_shown", shown)
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
            .remove("target_popup_shown") // Reset popup
            .remove("over_popup_shown")   // Reset popup
            .apply()
    }

    fun clearAllData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
