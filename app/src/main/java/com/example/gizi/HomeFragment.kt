package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.databinding.FragmentHomeBinding
import com.example.gizi.helper.PrefsHelper
import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val historyAdapter = HomeHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        tampilkanDataUser()
        loadNutrisiHariIni()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupListeners() {
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
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        db.collection("users").document(uid)
                            .collection("nutrition_logs").document(today)
                            .delete()
                    }
                    loadNutrisiHariIni()
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
        tampilkanDataUser()
        loadNutrisiHariIni()
    }

    private fun tampilkanDataUser() {
        val prefs = requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
        val name = prefs.getString("user_name", "Sobat") ?: "Sobat"
        val target = PrefsHelper.getTargetKalori(requireContext())

        binding.tvGreeting.text = "Halo, ${name.split(" ").first()}! 👋"
        binding.tvTargetKalori.text = "$target kkal"
        binding.tvInitial.text = name.firstOrNull()?.toString()?.uppercase() ?: "G"

        val fotoUri = PrefsHelper.getFotoProfil(requireContext())
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
    }

    private fun loadNutrisiHariIni() {
        if (_binding == null) return
        val ctx = requireContext()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        updateUIFromLocal(ctx)

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("nutrition_logs").document(today)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                if (!document.exists()) {
                    updateUI(0f, 0f, 0f, 0f, emptyList())
                    return@addOnSuccessListener
                }

                val kalori = (document.getDouble("totalKalori") ?: 0.0).toFloat()
                val protein = (document.getDouble("totalProtein") ?: 0.0).toFloat()
                val karbo = (document.getDouble("totalKarbo") ?: 0.0).toFloat()
                val lemak = (document.getDouble("totalLemak") ?: 0.0).toFloat()

                @Suppress("UNCHECKED_CAST")
                val makananRaw = document.get("makanan") as? List<Map<String, Any>> ?: emptyList()
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

                PrefsHelper.simpanKalori(ctx, kalori, protein, karbo, lemak, foods)
                updateUI(kalori, protein, karbo, lemak, foods)
            }
            .addOnFailureListener { updateUIFromLocal(ctx) }
    }

    private fun updateUIFromLocal(ctx: Context) {
        updateUI(
            PrefsHelper.getKaloriHariIni(ctx),
            PrefsHelper.getProteinHariIni(ctx),
            PrefsHelper.getKarboHariIni(ctx),
            PrefsHelper.getLemakHariIni(ctx),
            PrefsHelper.getFoodHistory(ctx)
        )
    }

    private fun updateUI(kalori: Float, protein: Float, karbo: Float, lemak: Float, history: List<FoodItem>) {
        if (!isAdded || _binding == null) return
        val target = PrefsHelper.getTargetKalori(requireContext())
        val sisa = (target - kalori).coerceAtLeast(0f)
        val progress = if (target > 0) ((kalori / target) * 100).toInt().coerceIn(0, 100) else 0

        binding.tvKaloriDikonsumsi.text = "${kalori.toInt()} kkal"
        binding.tvKaloriSisa.text = "${sisa.toInt()} kkal tersisa"
        binding.progressKalori.progress = progress

        val locale = Locale.getDefault()
        binding.tvProtein.text = "${String.format(locale, "%.1f", protein)} g"
        binding.tvKarbo.text = "${String.format(locale, "%.1f", karbo)} g"
        binding.tvLemak.text = "${String.format(locale, "%.1f", lemak)} g"

        val colorRes = when {
            progress >= 100 -> R.color.status_red
            progress >= 80  -> R.color.orange_light
            else            -> R.color.primary_green
        }
        binding.progressKalori.progressTintList = android.content.res.ColorStateList.valueOf(resources.getColor(colorRes, null))

        if (history.isEmpty()) {
            binding.cvHistoryOnboarding.isVisible = true
            binding.rvHistory.isVisible = false
            binding.btnLihatSemua.isVisible = false
            binding.btnResetKalori.isVisible = false
        } else {
            binding.cvHistoryOnboarding.isVisible = false
            binding.rvHistory.isVisible = true
            binding.btnResetKalori.isVisible = true
            val limitedHistory = if (history.size > 3) history.take(3) else history
            historyAdapter.submitList(limitedHistory)
            binding.btnLihatSemua.isVisible = history.size > 3
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
