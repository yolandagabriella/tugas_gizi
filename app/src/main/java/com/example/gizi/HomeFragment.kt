package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.databinding.FragmentHomeBinding
import com.example.gizi.helper.PrefsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bumptech.glide.Glide
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val historyAdapter = HomeHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        updateDashboard()
    }

    private fun setupRecyclerView() {
        if (_binding == null) return
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupListeners() {
        if (_binding == null) return
        binding.btnMainAction.setOnClickListener {
            findNavController().navigate(R.id.navigation_nutrition)
        }
        binding.btnSearchNow.setOnClickListener {
            findNavController().navigate(R.id.navigation_nutrition)
        }
        binding.btnLihatSemua.setOnClickListener {
            showAllFoodBottomSheet()
        }
        binding.btnResetKalori.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Data?")
                .setMessage("Semua data makanan hari ini akan dihapus. Lanjutkan?")
                .setPositiveButton("Reset") { _, _ ->
                    PrefsHelper.clearData(requireContext())
                    updateDashboard()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showAllFoodBottomSheet() {
        if (!isAdded || _binding == null) return
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_food_history, binding.root, false)
        val rvAllFood = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_all_food)
        
        val adapter = HomeHistoryAdapter()
        rvAllFood.layoutManager = LinearLayoutManager(requireContext())
        rvAllFood.adapter = adapter
        adapter.submitList(PrefsHelper.getFoodHistory(requireContext()))
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val ctx = context ?: return
        if (_binding == null) return
        
        // Ambil nama user dari SharedPreferences
        val userPrefs = ctx.getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
        val userName = userPrefs.getString("user_name", "User")
        binding.tvUserGreeting.text = getString(R.string.halo_user_format, userName)
        binding.tvInitial.text = userName?.firstOrNull()?.toString()?.uppercase() ?: "G"

        // Tampilkan foto profil kalau ada
        val fotoUri = PrefsHelper.getFotoProfil(ctx)
        if (fotoUri.isNotEmpty()) {
            binding.ivFotoHome.isVisible = true
            binding.tvInitial.isVisible = false
            
            Glide.with(this)
                .load(if (fotoUri.startsWith("/")) java.io.File(fotoUri) else fotoUri)
                .error(R.color.primary_green)
                .into(binding.ivFotoHome)
        } else {
            binding.ivFotoHome.isVisible = false
            binding.tvInitial.isVisible = true
        }

        val kaloriHariIni = PrefsHelper.getKaloriHariIni(ctx)
        val targetKalori = PrefsHelper.getTargetKalori(ctx).toFloat()
        val protein = PrefsHelper.getProteinHariIni(ctx)
        val karbo = PrefsHelper.getKarboHariIni(ctx)
        val lemak = PrefsHelper.getLemakHariIni(ctx)
        val foodHistory = PrefsHelper.getFoodHistory(ctx)

        // Update Nutrient Values
        binding.tvProteinValue.text = getString(R.string.nutrition_value_unit_format, protein.toDouble())
        binding.tvCarbsValue.text = getString(R.string.nutrition_value_unit_format, karbo.toDouble())
        binding.tvFatValue.text = getString(R.string.nutrition_value_unit_format, lemak.toDouble())

        if (foodHistory.isEmpty()) {
            // State KOSONG
            binding.llEmptyData.isVisible = true
            binding.pbCalories.isVisible = false
            binding.llCalorieCenter.isVisible = false
            binding.llCalorieInfo.isVisible = false
            binding.cvHistoryOnboarding.isVisible = true
            binding.rvHistory.isVisible = false
            binding.btnLihatSemua.isVisible = false
            binding.btnResetKalori.isVisible = false
        } else {
            // State BERISI
            binding.llEmptyData.isVisible = false
            binding.pbCalories.isVisible = true
            binding.llCalorieCenter.isVisible = true
            binding.llCalorieInfo.isVisible = true
            binding.cvHistoryOnboarding.isVisible = false
            binding.rvHistory.isVisible = true
            binding.btnResetKalori.isVisible = true

            // Update angka kalori
            binding.tvCaloriesValue.text = String.format(Locale.getDefault(), "%.0f", kaloriHariIni)
            val remaining = (targetKalori - kaloriHariIni).coerceAtLeast(0f)
            binding.tvCaloriesRemaining.text = getString(R.string.kkal_remaining_format, remaining.toDouble())

            // Update progress circle (0 - 100)
            val progress = (kaloriHariIni / targetKalori * 100).toInt()
            binding.pbCalories.progress = progress.coerceIn(0, 100)

            // Warna berdasarkan progress
            val color = when {
                progress >= 100 -> "#4CAF50" // Hijau — target tercapai!
                progress >= 90  -> "#FF9800" // Orange — hampir tercapai
                progress >= 50  -> "#FFC107" // Kuning — setengah jalan
                else            -> "#2196F3" // Biru — masih awal
            }
            binding.pbCalories.progressDrawable.setTint(color.toColorInt())

            // Cek status kalori
            val sudahMelewati = kaloriHariIni > targetKalori
            val sudahTercapai = kaloriHariIni >= targetKalori && kaloriHariIni <= targetKalori * 1.05f

            when {
                sudahTercapai -> {
                    // Pop up selamat
                    if (!PrefsHelper.isTargetPopupShown(requireContext())) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("🎉 Selamat!")
                            .setMessage("Kamu sudah mencapai target kalori harian kamu, Sobat Gizigo! Pertahankan terus ya!")
                            .setPositiveButton("Yeay!") { _, _ ->
                                PrefsHelper.setTargetPopupShown(requireContext(), true)
                            }
                            .show()
                    }
                }
                sudahMelewati -> {
                    // Pop up peringatan
                    if (!PrefsHelper.isOverPopupShown(requireContext())) {
                        val lebih = (kaloriHariIni - targetKalori).toInt()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("⚠️ Peringatan!")
                            .setMessage("Kamu sudah melebihi target kalori sebanyak ${lebih} kkal hari ini! Coba kurangi porsi makan berikutnya ya!")
                            .setPositiveButton("Oke, Mengerti") { _, _ ->
                                PrefsHelper.setOverPopupShown(requireContext(), true)
                            }
                            .show()
                    }
                }
            }

            // Tampilkan max 3 item saja di home
            val limitedHistory = if (foodHistory.size > 3) foodHistory.take(3) else foodHistory
            historyAdapter.submitList(limitedHistory)
            
            // Tampilkan tombol "Lihat Semua" jika lebih dari 3
            binding.btnLihatSemua.isVisible = foodHistory.size > 3
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
