package com.example.gizi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.databinding.FragmentNutritionBinding
import com.example.gizi.helper.PrefsHelper
import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient
import com.example.gizi.viewmodel.NutrisiViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        viewModel.initTarget(requireContext())
        setupSpinners()
        setupRecyclerViews()
        setupSearchBar()
        setupListeners()
        observeViewModel()
    }

    private fun setupSpinners() {
        val categories = arrayOf("Makanan Pokok", "Lauk Pauk", "Sayuran", "Buah")
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories,
        )
        val portionUnits = arrayOf(
            // Umum
            "1 Porsi",
            "1 Piring",
            "1 Mangkok",
            "1 Gelas",
            "1 Cangkir",

            // Makanan Pokok
            "1 Centong Nasi",
            "1 Lembar Roti",
            "1 Bungkus Mie",

            // Lauk & Protein
            "1 Potong Sedang",
            "1 Potong Besar",
            "1 Butir Telur",
            "1 Ekor Ikan",

            // Sayuran & Buah
            "1 Buah Sedang",
            "1 Buah Besar",
            "1 Ikat",
            "1 Genggam",

            // Minuman
            "1 Botol (600ml)",
            "1 Sachet",

            // Takaran Kecil
            "1 Sendok Makan",
            "1 Sendok Teh",
            "1 Bungkus Kecil",
        )
        binding.spPortionUnit.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            portionUnits,
        )

        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val satuanDefault = when (pos) {
                    0 -> 1  // Makanan Pokok → 1 Piring
                    1 -> 8  // Lauk Pauk → 1 Potong Sedang
                    2 -> 2  // Sayuran → 1 Mangkok
                    3 -> 12 // Buah → 1 Buah Sedang
                    else -> 0
                }
                binding.spPortionUnit.setSelection(satuanDefault)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spPortionUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // Isi gram default otomatis sesuai satuan porsi
                val gramDefault = when (pos) {
                    0  -> "250"  // 1 Porsi
                    1  -> "200"  // 1 Piring
                    2  -> "200"  // 1 Mangkok
                    3  -> "200"  // 1 Gelas
                    4  -> "150"  // 1 Cangkir
                    5  -> "100"  // 1 Centong Nasi
                    6  -> "30"   // 1 Lembar Roti
                    7  -> "85"   // 1 Bungkus Mie
                    8  -> "75"   // 1 Potong Sedang
                    9  -> "150"  // 1 Potong Besar
                    10 -> "60"   // 1 Butir Telur
                    11 -> "200"  // 1 Ekor Ikan
                    12 -> "100"  // 1 Buah Sedang
                    13 -> "200"  // 1 Buah Besar
                    14 -> "100"  // 1 Ikat
                    15 -> "50"   // 1 Genggam
                    16 -> "600"  // 1 Botol
                    17 -> "25"   // 1 Sachet
                    18 -> "15"   // 1 Sendok Makan
                    19 -> "5"    // 1 Sendok Teh
                    20 -> "50"   // 1 Bungkus Kecil
                    else -> "100"
                }
                binding.etGramWeight.setText(gramDefault)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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
            
            // Cek kalau ga ada makanan yang ditambah
            if (viewModel.selectedFoods.value.isNullOrEmpty()) {
                MaterialAlertDialogBuilder(ctx)
                    .setTitle("Oops!")
                    .setMessage(getString(R.string.belum_tambah_makanan))
                    .setPositiveButton("Oke") { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            val kaloriTersimpan = PrefsHelper.getKaloriHariIni(ctx)
            val totalSekarang = kaloriTersimpan + viewModel.totalKalori
            val sisa = viewModel.targetKalori - totalSekarang

            if (sisa < 0) {
                val lebih = Math.abs(sisa.toInt())
                MaterialAlertDialogBuilder(ctx)
                    .setTitle("⚠️ Peringatan!")
                    .setMessage(getString(R.string.yakin_simpan_lebih, lebih))
                    .setPositiveButton("Simpan Tetap") { _, _ ->
                        viewModel.simpanKaloriHarian(ctx)
                        Toast.makeText(ctx, "Data nutrisi hari ini tersimpan!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Batal, Kurangi Dulu", null)
                    .show()
            } else if (totalSekarang >= viewModel.targetKalori * 0.95) {
                MaterialAlertDialogBuilder(ctx)
                    .setTitle("🎉 Selamat!")
                    .setMessage(getString(R.string.target_tercapai_congrats))
                    .setPositiveButton("Simpan!") { _, _ ->
                        viewModel.simpanKaloriHarian(ctx)
                        Toast.makeText(ctx, "Data nutrisi hari ini tersimpan!", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            } else {
                viewModel.simpanKaloriHarian(ctx)
                Toast.makeText(ctx, "Data nutrisi hari ini tersimpan!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.etGramWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    // Kalau gram diisi, disable satuan porsi & jumlah
                    binding.spPortionUnit.isEnabled = false
                    binding.spPortionUnit.alpha = 0.4f
                    binding.etPortionCount.isEnabled = false
                    binding.etPortionCount.alpha = 0.4f
                } else {
                    // Kalau gram dikosongkan, enable lagi
                    binding.spPortionUnit.isEnabled = true
                    binding.spPortionUnit.alpha = 1f
                    binding.etPortionCount.isEnabled = true
                    binding.etPortionCount.alpha = 1f
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveCustomFood() {
        val name = binding.etFoodName.text.toString().trim()
        val category = binding.spCategory.selectedItem.toString()
        val portionCount = binding.etPortionCount.text.toString().toDoubleOrNull() ?: 1.0
        val gramInput = binding.etGramWeight.text.toString().toDoubleOrNull()

        if (name.isNotEmpty()) {
            val baseNutrients = when (category) {
                "Makanan Pokok" -> arrayOf(175.0, 40.0, 4.0, 0.0) // per porsi (~100g)
                "Lauk Pauk"     -> arrayOf(75.0, 0.0, 7.0, 5.0)  // per porsi (~50g)
                "Sayuran"       -> arrayOf(25.0, 5.0, 1.0, 0.0)  // per porsi (~100g)
                "Buah"          -> arrayOf(50.0, 12.0, 0.0, 0.0) // per porsi (~100g)
                else            -> arrayOf(0.0, 0.0, 0.0, 0.0)
            }

            // Jika ada input gram, sesuaikan pengalinya (asumsi baseNutrients adalah untuk ~100g)
            // Khusus Lauk Pauk asumsi porsi standar adalah 50g
            val multiplier = if (gramInput != null) {
                val baseWeight = if (category == "Lauk Pauk") 50.0 else 100.0
                gramInput / baseWeight
            } else {
                portionCount
            }

            val nutrients = listOf(
                FoodNutrient("Energy",       baseNutrients[0] * multiplier, "kcal"),
                FoodNutrient("Carbohydrate", baseNutrients[1] * multiplier, "g"),
                FoodNutrient("Protein",      baseNutrients[2] * multiplier, "g"),
                FoodNutrient("Total lipid",  baseNutrients[3] * multiplier, "g")
            )
            
            val foodNameWithWeight = if (gramInput != null) "$name (${gramInput.toInt()}g)" else name
            
            viewModel.addFood(FoodItem(
                fdcId = System.currentTimeMillis().toInt(),
                description = foodNameWithWeight,
                foodNutrients = nutrients
            ))
            
            binding.etFoodName.text.clear()
            binding.etPortionCount.setText("1")
            binding.etGramWeight.text.clear()
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

        viewModel.statusMessage.observe(viewLifecycleOwner) { pair ->
            pair?.let { (message, isAchievement) ->
                if (isAchievement) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Bagus Sekali! 🌟")
                        .setMessage(message)
                        .setPositiveButton("Reset & Mulai Baru") { _, _ ->
                            viewModel.resetMakanan(requireContext())
                        }
                        .setNegativeButton("Lanjutkan") { _, _ ->
                            viewModel.resetStatusMessage()
                        }
                        .show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    viewModel.resetStatusMessage()
                }
            }
        }
    }

    private fun updateTotalNutrisiUI() {
        // Ambil kalori yang udah tersimpan di beranda
        val kaloriTersimpan = PrefsHelper.getKaloriHariIni(requireContext())
        // Tambah kalori dari makanan yang baru dipilih di sesi ini
        val totalSekarang = kaloriTersimpan + viewModel.totalKalori
        val sisa = viewModel.targetKalori - totalSekarang

        if (sisa < 0) {
            binding.tvCalorieLabel.text = getString(R.string.exceed_target)
            binding.tvTotalCalories.text = getString(R.string.kkal_unit_format, Math.abs(sisa.toInt()))
            binding.tvTotalCalories.setTextColor("#FF5252".toColorInt())
        } else {
            binding.tvCalorieLabel.text = getString(R.string.sisa_target_label)
            binding.tvTotalCalories.text = getString(R.string.kkal_unit_format, sisa.toInt())
            binding.tvTotalCalories.setTextColor("#FFFFFF".toColorInt())
        }

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
