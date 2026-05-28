package com.example.gizi

import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient

object IndonesianFoodDatabase {

    val foods: List<FoodItem> = listOf(

        // === MAKANAN POKOK ===
        food(1, "Nasi Putih (1 centong ~100g)",
            130.0, 28.0, 2.7, 0.3),
        food(2, "Nasi Goreng (1 porsi ~200g)",
            260.0, 38.0, 6.0, 9.0),
        food(3, "Nasi Uduk (1 porsi ~150g)",
            210.0, 35.0, 4.0, 6.0),
        food(4, "Nasi Kuning (1 porsi ~150g)",
            195.0, 34.0, 3.5, 5.0),
        food(5, "Lontong (2 potong ~100g)",
            100.0, 22.0, 2.0, 0.5),
        food(6, "Ketupat (1 buah ~85g)",
            85.0, 19.0, 1.5, 0.3),
        food(7, "Bubur Ayam (1 mangkok ~300g)",
            180.0, 28.0, 10.0, 3.5),
        food(8, "Mie Goreng (1 porsi ~200g)",
            330.0, 45.0, 8.0, 13.0),
        food(9, "Mie Rebus (1 porsi ~200g)",
            240.0, 38.0, 7.0, 6.0),
        food(10, "Bihun Goreng (1 porsi ~150g)",
            255.0, 42.0, 5.0, 7.0),

        // === LAUK PAUK ===
        food(11, "Ayam Goreng (1 potong ~100g)",
            246.0, 0.0, 27.0, 15.0),
        food(12, "Ayam Bakar (1 potong ~100g)",
            195.0, 2.0, 28.0, 8.0),
        food(13, "Rendang Sapi (1 potong ~75g)",
            210.0, 4.0, 22.0, 12.0),
        food(14, "Ikan Goreng (1 ekor sedang ~100g)",
            195.0, 0.0, 24.0, 10.0),
        food(15, "Ikan Bakar (1 ekor sedang ~100g)",
            155.0, 0.0, 25.0, 5.5),
        food(16, "Telur Goreng (1 butir)",
            92.0, 0.4, 6.3, 7.2),
        food(17, "Telur Rebus (1 butir)",
            78.0, 0.6, 6.3, 5.3),
        food(18, "Telur Dadar (1 butir)",
            95.0, 0.5, 6.5, 7.5),
        food(19, "Tempe Goreng (1 potong ~50g)",
            100.0, 5.0, 9.0, 5.5),
        food(20, "Tempe Bacem (1 potong ~50g)",
            115.0, 10.0, 8.5, 5.0),
        food(21, "Tahu Goreng (1 potong ~60g)",
            85.0, 2.0, 7.0, 5.5),
        food(22, "Tahu Bacem (1 potong ~60g)",
            95.0, 6.0, 7.5, 5.0),
        food(23, "Bakso (5 butir ~100g)",
            175.0, 10.0, 14.0, 8.0),
        food(24, "Sate Ayam (5 tusuk ~100g)",
            210.0, 5.0, 22.0, 11.0),
        food(25, "Sate Kambing (5 tusuk ~100g)",
            225.0, 3.0, 23.0, 13.0),
        food(26, "Ikan Tuna Kaleng (1/2 kaleng ~85g)",
            130.0, 0.0, 25.0, 3.0),
        food(27, "Udang Goreng (5 ekor ~75g)",
            140.0, 2.0, 18.0, 6.5),

        // === SAYURAN ===
        food(28, "Sayur Bayam (1 mangkok ~100g)",
            36.0, 3.6, 3.5, 0.7),
        food(29, "Tumis Kangkung (1 porsi ~100g)",
            55.0, 4.0, 2.5, 3.0),
        food(30, "Capcay (1 porsi ~150g)",
            85.0, 8.0, 4.0, 4.0),
        food(31, "Sayur Asem (1 mangkok ~200g)",
            70.0, 12.0, 3.0, 1.0),
        food(32, "Tumis Buncis (1 porsi ~100g)",
            65.0, 7.0, 2.5, 3.0),
        food(33, "Sup Sayuran (1 mangkok ~250g)",
            75.0, 10.0, 3.5, 2.0),
        food(34, "Lalapan (timun+tomat+selada ~100g)",
            30.0, 5.5, 1.5, 0.3),
        food(35, "Terong Balado (1 porsi ~100g)",
            80.0, 7.0, 2.0, 5.0),

        // === MAKANAN BERKUAH ===
        food(36, "Soto Ayam (1 mangkok ~300g)",
            215.0, 12.0, 18.0, 9.0),
        food(37, "Sop Buntut (1 mangkok ~300g)",
            285.0, 8.0, 22.0, 18.0),
        food(38, "Rawon (1 mangkok ~300g)",
            260.0, 10.0, 20.0, 15.0),
        food(39, "Opor Ayam (1 porsi ~200g)",
            290.0, 6.0, 22.0, 20.0),
        food(40, "Gado-gado (1 porsi ~250g)",
            310.0, 25.0, 14.0, 18.0),
        food(41, "Pecel (1 porsi ~200g)",
            265.0, 22.0, 11.0, 15.0),

        // === BUAH-BUAHAN ===
        food(42, "Pisang Ambon (1 buah ~100g)",
            99.0, 23.0, 1.1, 0.3),
        food(43, "Pisang Goreng (1 buah ~80g)",
            145.0, 25.0, 1.5, 4.5),
        food(44, "Mangga (1/2 buah ~150g)",
            98.0, 24.5, 1.4, 0.4),
        food(45, "Pepaya (1 potong ~150g)",
            65.0, 15.0, 0.9, 0.4),
        food(46, "Semangka (2 potong ~200g)",
            60.0, 15.0, 1.2, 0.3),
        food(47, "Jeruk (1 buah ~130g)",
            60.0, 15.0, 1.2, 0.2),
        food(48, "Apel (1 buah ~150g)",
            78.0, 21.0, 0.4, 0.2),
        food(49, "Melon (2 potong ~200g)",
            64.0, 16.0, 1.0, 0.3),
        food(50, "Alpukat (1/2 buah ~75g)",
            120.0, 6.0, 1.5, 11.0),

        // === CAMILAN & JAJANAN ===
        food(51, "Gorengan Bakwan (1 buah ~60g)",
            145.0, 16.0, 3.5, 7.5),
        food(52, "Martabak Telur (1 potong ~100g)",
            265.0, 22.0, 12.0, 14.0),
        food(53, "Risoles (1 buah ~70g)",
            155.0, 18.0, 5.0, 7.0),
        food(54, "Roti Tawar (2 lembar ~60g)",
            158.0, 29.0, 5.4, 2.2),
        food(55, "Kerupuk (5 keping ~15g)",
            72.0, 12.0, 0.8, 2.0),

        // === MINUMAN ===
        food(56, "Es Teh Manis (1 gelas ~250ml)",
            90.0, 22.0, 0.1, 0.0),
        food(57, "Jus Alpukat (1 gelas ~300ml)",
            235.0, 24.0, 3.0, 14.0),
        food(58, "Jus Jeruk (1 gelas ~250ml)",
            112.0, 26.0, 1.7, 0.5),
        food(59, "Susu Sapi (1 gelas ~200ml)",
            122.0, 9.6, 6.4, 4.8),
        food(60, "Kopi Susu (1 gelas ~200ml)",
            85.0, 10.0, 2.5, 3.5)
    )

    // Helper function buat buat FoodItem lebih ringkas
    private fun food(
        id: Int, nama: String,
        kalori: Double, karbo: Double,
        protein: Double, lemak: Double
    ): FoodItem {
        return FoodItem(
            fdcId = id,
            description = nama,
            foodNutrients = listOf(
                FoodNutrient("Energy", kalori, "kcal"),
                FoodNutrient("Carbohydrate", karbo, "g"),
                FoodNutrient("Protein", protein, "g"),
                FoodNutrient("Total lipid", lemak, "g")
            )
        )
    }

    // Fungsi search dari database lokal
    fun search(query: String): List<FoodItem> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase().trim()
        return foods.filter { it.description.lowercase().contains(q) }
    }
}
