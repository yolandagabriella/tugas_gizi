package com.example.gizi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.databinding.FragmentNutritionBinding
import com.example.gizi.helper.PrefsHelper
import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient
import com.example.gizi.viewmodel.NutrisiViewModel

class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NutrisiViewModel by viewModels()

    private lateinit var selectedFoodAdapter: SelectedFoodAdapter
    private lateinit var searchResultAdapter: FoodSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadExistingData() // Muat data yang sudah tersimpan hari ini
        setupSpinners()
        setupRecyclerViews()
        setupSearchBar()
        setupListeners()
        observeViewModel()
    }

    private fun loadExistingData() {
        val ctx = requireContext()
        val existingFoods = PrefsHelper.getFoodHistory(ctx)
        if (existingFoods.isNotEmpty()) {
            existingFoods.forEach { viewModel.addFood(it) }
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf("Makanan Pokok", "Lauk Pauk", "Sayuran", "Buah")
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories,
        )
        val portionUnits = arrayOf(
            "1 Piring/Centong", "1 Potong Sedang",
            "1 Mangkok", "1 Buah Sedang", "1 Sendok Makan",
        )
        binding.spPortionUnit.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            portionUnits,
        )
    }

    private fun setupRecyclerViews() {
        // Adapter untuk hasil pencarian (API + Lokal)
        searchResultAdapter = FoodSearchAdapter { food ->
            viewModel.addFood(food)
            binding.etSearchFood.text.clear()
            binding.rvFoodList.isVisible = false
            Toast.makeText(
                requireContext(),
                "${food.description} ditambahkan!",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvFoodList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFoodList.adapter = searchResultAdapter

        // Adapter untuk makanan yang sudah dipilih
        selectedFoodAdapter = SelectedFoodAdapter { food ->
            viewModel.removeFood(food)
        }
        binding.rvSelectedFood.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedFood.adapter = selectedFoodAdapter
    }

    private fun setupSearchBar() {
        binding.etSearchFood.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 3) {
                    viewModel.searchFood(query)
                    binding.rvFoodList.isVisible = true
                } else {
                    searchResultAdapter.submitList(emptyList())
                    binding.rvFoodList.isVisible = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupListeners() {
        binding.btnShowAddFood.setOnClickListener {
            binding.cvAddFoodForm.isVisible = !binding.cvAddFoodForm.isVisible
        }

        binding.btnSaveFood.setOnClickListener {
            saveCustomFood()
        }

        binding.btnSimpanHarian.setOnClickListener {
            val ctx = requireContext()
            viewModel.simpanKaloriHarian(ctx)
            Toast.makeText(ctx, "✅ Data nutrisi hari ini tersimpan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCustomFood() {
        val name = binding.etFoodName.text.toString()
        val category = binding.spCategory.selectedItem.toString()
        val portionCount = binding.etPortionCount.text.toString().toDoubleOrNull() ?: 1.0

        if (name.isNotEmpty()) {
            val baseNutrients = when (category) {
                "Makanan Pokok" -> arrayOf(175.0, 40.0, 4.0, 0.0)
                "Lauk Pauk"     -> arrayOf(75.0, 0.0, 7.0, 5.0)
                "Sayuran"       -> arrayOf(25.0, 5.0, 1.0, 0.0)
                "Buah"          -> arrayOf(50.0, 12.0, 0.0, 0.0)
                else            -> arrayOf(0.0, 0.0, 0.0, 0.0)
            }
            val nutrients = listOf(
                FoodNutrient("Energy",       baseNutrients[0] * portionCount, "kcal"),
                FoodNutrient("Carbohydrate", baseNutrients[1] * portionCount, "g"),
                FoodNutrient("Protein",      baseNutrients[2] * portionCount, "g"),
                FoodNutrient("Total lipid",  baseNutrients[3] * portionCount, "g")
            )
            viewModel.addFood(FoodItem(
                fdcId = System.currentTimeMillis().toInt(),
                description = name,
                foodNutrients = nutrients
            ))
            binding.etFoodName.text.clear()
            binding.etPortionCount.setText("1")
            binding.cvAddFoodForm.isVisible = false
            Toast.makeText(requireContext(), "Makanan ditambahkan!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Nama makanan tidak boleh kosong",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { foods ->
            searchResultAdapter.submitList(foods)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.etSearchFood.hint = if (loading) "Mencari..." else "Cari makanan..."
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.selectedFoods.observe(viewLifecycleOwner) { foods ->
            selectedFoodAdapter.submitList(foods.toList())
            updateTotalNutrisiUI()
        }
    }

    private fun updateTotalNutrisiUI() {
        binding.tvTotalCalories.text = getString(
            R.string.nutrition_value_format, viewModel.totalKalori
        )
        binding.tvTotalProtein.text = getString(
            R.string.nutrition_value_unit_format, viewModel.totalProtein
        )
        binding.tvTotalCarbs.text = getString(
            R.string.nutrition_value_unit_format, viewModel.totalKarbo
        )
        binding.tvTotalFat.text = getString(
            R.string.nutrition_value_unit_format, viewModel.totalLemak
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
