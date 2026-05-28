package com.example.gizi.helper

import android.content.Context
import com.example.gizi.model.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrefsHelper {

    private const val PREFS_NAME = "gizigo_prefs"
    private val gson = Gson()

    fun simpanKalori(context: Context, kalori: Float, protein: Float, karbo: Float, lemak: Float, foods: List<FoodItem> = emptyList()) {
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
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tanggalSimpan = prefs.getString("tanggal_simpan", "")

        return if (tanggalSimpan == today) {
            prefs.getFloat("kalori_hari_ini", 0f)
        } else {
            0f
        }
    }

    fun getFoodHistory(context: Context): List<FoodItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tanggalSimpan = prefs.getString("tanggal_simpan", "")
        
        if (tanggalSimpan != today) return emptyList()

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

    fun getTargetKalori(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("target_kalori", 2000)
    }

    fun simpanTargetKalori(context: Context, target: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt("target_kalori", target)
            .apply()
    }

    fun clearData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
