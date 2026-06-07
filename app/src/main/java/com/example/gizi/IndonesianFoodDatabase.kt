package com.example.gizi

import com.example.gizi.model.FoodItem
import com.example.gizi.model.FoodNutrient

object IndonesianFoodDatabase {

    val foods: List<FoodItem> = listOf(

        // === MAKANAN POKOK ===
        food(1, "Nasi Putih (1 centong ~100g)", 130.0, 28.0, 2.7, 0.3),
        food(2, "Nasi Putih (1 piring ~200g)", 260.0, 56.0, 5.4, 0.6),
        food(3, "Nasi Putih (1 piring besar ~300g)", 390.0, 84.0, 8.1, 0.9),
        food(4, "Nasi Goreng (1 porsi ~200g)", 260.0, 38.0, 6.0, 9.0),
        food(5, "Nasi Uduk (1 porsi ~150g)", 210.0, 35.0, 4.0, 6.0),
        food(6, "Nasi Kuning (1 porsi ~150g)", 195.0, 34.0, 3.5, 5.0),
        food(7, "Lontong (1 potong ~50g)", 50.0, 11.0, 1.0, 0.3),
        food(8, "Lontong (2 potong ~100g)", 100.0, 22.0, 2.0, 0.5),
        food(9, "Ketupat (1 buah ~85g)", 85.0, 19.0, 1.5, 0.3),
        food(10, "Bubur Ayam (1 mangkok ~300g)", 180.0, 28.0, 10.0, 3.5),
        food(11, "Mie Goreng (1 porsi ~200g)", 330.0, 45.0, 8.0, 13.0),
        food(12, "Mie Rebus (1 porsi ~200g)", 240.0, 38.0, 7.0, 6.0),
        food(13, "Bihun Goreng (1 porsi ~150g)", 255.0, 42.0, 5.0, 7.0),
        food(14, "Roti Tawar (1 lembar ~30g)", 79.0, 14.5, 2.7, 1.1),
        food(15, "Roti Tawar (2 lembar ~60g)", 158.0, 29.0, 5.4, 2.2),
        food(16, "Roti Tawar (3 lembar ~90g)", 237.0, 43.5, 8.1, 3.3),
        food(17, "Roti Bakar (1 lembar ~30g)", 85.0, 15.0, 2.8, 1.8),
        food(18, "Roti Bakar (2 lembar ~60g)", 170.0, 30.0, 5.6, 3.6),
        food(19, "Kentang Rebus (1 buah sedang ~150g)", 116.0, 27.0, 2.5, 0.1),
        food(20, "Kentang Goreng (1 porsi ~100g)", 312.0, 41.0, 3.4, 15.0),
        food(21, "Singkong Rebus (1 potong ~100g)", 154.0, 36.0, 1.2, 0.3),
        food(22, "Ubi Rebus (1 buah sedang ~100g)", 86.0, 20.0, 1.6, 0.1),

        // === LAUK PAUK ===
        food(23, "Ayam Goreng (1 potong ~100g)", 246.0, 0.0, 27.0, 15.0),
        food(24, "Ayam Goreng (1/2 ekor ~250g)", 615.0, 0.0, 67.5, 37.5),
        food(25, "Ayam Bakar (1 potong ~100g)", 195.0, 2.0, 28.0, 8.0),
        food(26, "Rendang Sapi (1 potong ~75g)", 210.0, 4.0, 22.0, 12.0),
        food(27, "Rendang Sapi (2 potong ~150g)", 420.0, 8.0, 44.0, 24.0),
        food(28, "Ikan Goreng (1 ekor kecil ~80g)", 156.0, 0.0, 19.2, 8.0),
        food(29, "Ikan Goreng (1 ekor sedang ~100g)", 195.0, 0.0, 24.0, 10.0),
        food(30, "Ikan Bakar (1 ekor sedang ~100g)", 155.0, 0.0, 25.0, 5.5),
        food(31, "Telur Goreng (1 butir ~60g)", 92.0, 0.4, 6.3, 7.2),
        food(32, "Telur Goreng (2 butir ~120g)", 184.0, 0.8, 12.6, 14.4),
        food(33, "Telur Rebus (1 butir ~60g)", 78.0, 0.6, 6.3, 5.3),
        food(34, "Telur Rebus (2 butir ~120g)", 156.0, 1.2, 12.6, 10.6),
        food(35, "Telur Dadar (1 butir ~60g)", 95.0, 0.5, 6.5, 7.5),
        food(36, "Tempe Goreng (1 potong ~50g)", 100.0, 5.0, 9.0, 5.5),
        food(37, "Tempe Goreng (2 potong ~100g)", 200.0, 10.0, 18.0, 11.0),
        food(38, "Tempe Bacem (1 potong ~50g)", 115.0, 10.0, 8.5, 5.0),
        food(39, "Tahu Goreng (1 potong ~60g)", 85.0, 2.0, 7.0, 5.5),
        food(40, "Tahu Goreng (2 potong ~120g)", 170.0, 4.0, 14.0, 11.0),
        food(41, "Tahu Bacem (1 potong ~60g)", 95.0, 6.0, 7.5, 5.0),
        food(42, "Bakso (3 butir ~60g)", 105.0, 6.0, 8.4, 4.8),
        food(43, "Bakso (5 butir ~100g)", 175.0, 10.0, 14.0, 8.0),
        food(44, "Bakso (10 butir ~200g)", 350.0, 20.0, 28.0, 16.0),
        food(45, "Sate Ayam (3 tusuk ~60g)", 126.0, 3.0, 13.2, 6.6),
        food(46, "Sate Ayam (5 tusuk ~100g)", 210.0, 5.0, 22.0, 11.0),
        food(47, "Sate Kambing (5 tusuk ~100g)", 225.0, 3.0, 23.0, 13.0),
        food(48, "Ikan Tuna Kaleng (1/2 kaleng ~85g)", 130.0, 0.0, 25.0, 3.0),
        food(49, "Udang Goreng (5 ekor ~75g)", 140.0, 2.0, 18.0, 6.5),
        food(50, "Udang Goreng (10 ekor ~150g)", 280.0, 4.0, 36.0, 13.0),
        food(51, "Daging Sapi Goreng (1 potong ~100g)", 250.0, 0.0, 26.0, 16.0),
        food(52, "Ikan Lele Goreng (1 ekor ~150g)", 270.0, 0.0, 32.0, 15.0),
        food(53, "Ikan Salmon (1 potong ~100g)", 208.0, 0.0, 20.0, 13.0),
        food(54, "Cumi Goreng (1 porsi ~100g)", 175.0, 8.0, 18.0, 7.0),

        // === SAYURAN ===
        food(55, "Sayur Bayam (1 sendok makan ~25g)", 9.0, 0.9, 0.9, 0.2),
        food(56, "Sayur Bayam (1 mangkok ~100g)", 36.0, 3.6, 3.5, 0.7),
        food(57, "Tumis Kangkung (1 porsi ~100g)", 55.0, 4.0, 2.5, 3.0),
        food(58, "Capcay (1 porsi ~150g)", 85.0, 8.0, 4.0, 4.0),
        food(59, "Sayur Asem (1 mangkok ~200g)", 70.0, 12.0, 3.0, 1.0),
        food(60, "Tumis Buncis (1 porsi ~100g)", 65.0, 7.0, 2.5, 3.0),
        food(61, "Sup Sayuran (1 mangkok ~250g)", 75.0, 10.0, 3.5, 2.0),
        food(62, "Lalapan (timun+tomat+selada ~100g)", 30.0, 5.5, 1.5, 0.3),
        food(63, "Terong Balado (1 porsi ~100g)", 80.0, 7.0, 2.0, 5.0),
        food(64, "Tumis Toge (1 porsi ~100g)", 45.0, 5.0, 2.5, 2.0),
        food(65, "Tumis Brokoli (1 porsi ~100g)", 55.0, 6.0, 3.5, 2.0),
        food(66, "Wortel Rebus (1 buah ~80g)", 33.0, 7.7, 0.7, 0.1),
        food(67, "Jagung Rebus (1 buah ~150g)", 132.0, 29.0, 4.5, 1.8),

        // === MAKANAN BERKUAH ===
        food(68, "Soto Ayam (1 mangkok ~300g)", 215.0, 12.0, 18.0, 9.0),
        food(69, "Sop Buntut (1 mangkok ~300g)", 285.0, 8.0, 22.0, 18.0),
        food(70, "Rawon (1 mangkok ~300g)", 260.0, 10.0, 20.0, 15.0),
        food(71, "Opor Ayam (1 porsi ~200g)", 290.0, 6.0, 22.0, 20.0),
        food(72, "Gado-gado (1 porsi ~250g)", 310.0, 25.0, 14.0, 18.0),
        food(73, "Pecel (1 porsi ~200g)", 265.0, 22.0, 11.0, 15.0),
        food(74, "Pempek (1 buah ~100g)", 190.0, 28.0, 10.0, 4.5),
        food(75, "Pempek (2 buah ~200g)", 380.0, 56.0, 20.0, 9.0),
        food(76, "Ketoprak (1 porsi ~250g)", 285.0, 35.0, 12.0, 11.0),

        // === BUAH-BUAHAN ===
        food(77, "Pisang Ambon (1 buah kecil ~80g)", 79.0, 18.4, 0.9, 0.2),
        food(78, "Pisang Ambon (1 buah sedang ~100g)", 99.0, 23.0, 1.1, 0.3),
        food(79, "Pisang Ambon (1 buah besar ~120g)", 119.0, 27.6, 1.3, 0.4),
        food(80, "Pisang Goreng (1 buah ~80g)", 145.0, 25.0, 1.5, 4.5),
        food(81, "Pisang Goreng (2 buah ~160g)", 290.0, 50.0, 3.0, 9.0),
        food(82, "Mangga (1/4 buah ~75g)", 49.0, 12.3, 0.7, 0.2),
        food(83, "Mangga (1/2 buah ~150g)", 98.0, 24.5, 1.4, 0.4),
        food(84, "Mangga (1 buah ~300g)", 196.0, 49.0, 2.8, 0.8),
        food(85, "Pepaya (1 potong kecil ~100g)", 43.0, 10.0, 0.6, 0.3),
        food(86, "Pepaya (1 potong sedang ~150g)", 65.0, 15.0, 0.9, 0.4),
        food(87, "Semangka (1 potong ~100g)", 30.0, 7.5, 0.6, 0.2),
        food(88, "Semangka (2 potong ~200g)", 60.0, 15.0, 1.2, 0.3),
        food(89, "Jeruk (1 buah kecil ~100g)", 47.0, 12.0, 0.9, 0.1),
        food(90, "Jeruk (1 buah sedang ~130g)", 60.0, 15.0, 1.2, 0.2),
        food(91, "Apel (1 buah kecil ~100g)", 52.0, 14.0, 0.3, 0.2),
        food(92, "Apel (1 buah sedang ~150g)", 78.0, 21.0, 0.4, 0.2),
        food(93, "Melon (1 potong ~100g)", 32.0, 8.0, 0.5, 0.2),
        food(94, "Melon (2 potong ~200g)", 64.0, 16.0, 1.0, 0.3),
        food(95, "Alpukat (1/4 buah ~37g)", 60.0, 3.0, 0.75, 5.5),
        food(96, "Alpukat (1/2 buah ~75g)", 120.0, 6.0, 1.5, 11.0),
        food(97, "Alpukat (1 buah ~150g)", 240.0, 12.0, 3.0, 22.0),
        food(98, "Stroberi (5 buah ~75g)", 24.0, 5.7, 0.5, 0.2),
        food(99, "Stroberi (10 buah ~150g)", 48.0, 11.5, 1.0, 0.5),
        food(100, "Anggur (10 buah ~100g)", 69.0, 18.0, 0.7, 0.2),
        food(101, "Nanas (1 potong ~100g)", 50.0, 13.0, 0.5, 0.1),
        food(102, "Durian (1 biji ~30g)", 43.0, 9.0, 0.6, 1.5),
        food(103, "Durian (3 biji ~90g)", 129.0, 27.0, 1.8, 4.5),

        // === CAMILAN & JAJANAN ===
        food(104, "Gorengan Bakwan (1 buah ~60g)", 145.0, 16.0, 3.5, 7.5),
        food(105, "Gorengan Bakwan (2 buah ~120g)", 290.0, 32.0, 7.0, 15.0),
        food(106, "Martabak Telur (1 potong ~100g)", 265.0, 22.0, 12.0, 14.0),
        food(107, "Martabak Manis (1 potong ~100g)", 290.0, 42.0, 6.0, 11.0),
        food(108, "Risoles (1 buah ~70g)", 155.0, 18.0, 5.0, 7.0),
        food(109, "Kerupuk (5 keping ~15g)", 72.0, 12.0, 0.8, 2.0),
        food(110, "Kerupuk (10 keping ~30g)", 144.0, 24.0, 1.6, 4.0),
        food(111, "Biskuit (2 keping ~20g)", 90.0, 13.0, 1.5, 3.5),
        food(112, "Biskuit (5 keping ~50g)", 225.0, 32.5, 3.75, 8.75),
        food(113, "Donat (1 buah ~60g)", 210.0, 27.0, 3.5, 10.0),
        food(114, "Onde-onde (1 buah ~50g)", 130.0, 20.0, 2.5, 4.5),
        food(115, "Klepon (3 buah ~60g)", 120.0, 24.0, 1.5, 2.0),
        food(116, "Lemper (1 buah ~80g)", 145.0, 26.0, 4.5, 3.0),
        food(117, "Tahu Bulat (3 buah ~60g)", 110.0, 5.0, 8.0, 7.0),
        food(118, "Cireng (3 buah ~60g)", 140.0, 24.0, 2.0, 4.0),
        food(119, "Batagor (1 porsi ~100g)", 220.0, 18.0, 12.0, 11.0),
        food(120, "Siomay (1 porsi ~150g)", 215.0, 20.0, 14.0, 8.5),

        // === MINUMAN ===
        food(121, "Air Putih (1 gelas ~250ml)", 0.0, 0.0, 0.0, 0.0),
        food(122, "Es Teh Manis (1 gelas ~250ml)", 90.0, 22.0, 0.1, 0.0),
        food(123, "Teh Tawar (1 gelas ~250ml)", 2.0, 0.5, 0.0, 0.0),
        food(124, "Kopi Hitam (1 cangkir ~200ml)", 5.0, 0.7, 0.3, 0.0),
        food(125, "Kopi Susu (1 gelas ~200ml)", 85.0, 10.0, 2.5, 3.5),
        food(126, "Kopi Susu Kekinian (1 gelas ~350ml)", 220.0, 35.0, 4.0, 7.0),
        food(127, "Jus Alpukat (1 gelas ~300ml)", 235.0, 24.0, 3.0, 14.0),
        food(128, "Jus Jeruk (1 gelas ~250ml)", 112.0, 26.0, 1.7, 0.5),
        food(129, "Jus Mangga (1 gelas ~250ml)", 120.0, 29.0, 1.0, 0.5),
        food(130, "Susu Sapi (1 gelas ~200ml)", 122.0, 9.6, 6.4, 4.8),
        food(131, "Susu Kental Manis (2 sdm ~30ml)", 98.0, 16.5, 2.4, 2.6),
        food(132, "Minuman Bersoda (1 kaleng ~330ml)", 140.0, 35.0, 0.0, 0.0),
        food(133, "Minuman Energi (1 kaleng ~250ml)", 110.0, 27.0, 1.0, 0.0),
        food(134, "Es Krim (1 scoop ~60g)", 130.0, 16.0, 2.2, 7.0),
        food(135, "Es Krim (2 scoop ~120g)", 260.0, 32.0, 4.4, 14.0),

        // === FAST FOOD ===
        food(136, "Nasi + Ayam Fast Food (1 paket)", 550.0, 65.0, 28.0, 18.0),
        food(137, "Burger (1 buah ~150g)", 295.0, 28.0, 17.0, 12.0),
        food(138, "Burger Double (1 buah ~250g)", 490.0, 35.0, 30.0, 25.0),
        food(139, "Pizza (1 slice ~100g)", 266.0, 33.0, 11.0, 10.0),
        food(140, "Pizza (2 slice ~200g)", 532.0, 66.0, 22.0, 20.0),
        food(141, "Kentang Goreng Kecil (~100g)", 312.0, 41.0, 3.4, 15.0),
        food(142, "Kentang Goreng Besar (~150g)", 468.0, 61.5, 5.1, 22.5),
        food(143, "Hot Dog (1 buah ~100g)", 290.0, 24.0, 11.0, 17.0),
        food(144, "Sandwich (1 buah ~150g)", 280.0, 32.0, 14.0, 10.0),
    )

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

    fun search(query: String): List<FoodItem> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase().trim()
        return foods.filter { it.description.lowercase().contains(q) }
    }
}