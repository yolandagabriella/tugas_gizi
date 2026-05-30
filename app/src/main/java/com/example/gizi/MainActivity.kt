package com.example.gizi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gizi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Aktifkan fitur Edge-to-Edge (Layar Penuh)
        enableEdgeToEdge()
        
        // 2. Inisialisasi View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Atur padding sistem agar UI tidak tertutup status bar/nav bar HP
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Padding bawah diatur 0 agar Bottom Navigation menempel di bawah dengan pas
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 4. Inisialisasi Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // 5. Hubungkan Bottom Navigation dengan NavController
        binding.bottomNav.setupWithNavController(navController)

        // 6. Atur kapan Menu Bawah muncul atau sembunyi
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Sembunyikan di layar awal/logon
                R.id.navigation_login, 
                R.id.navigation_register, 
                R.id.navigation_onboarding,
                R.id.navigation_onboarding_profile,
                -> {
                    binding.bottomNav.isVisible = false
                }
                // Tampilkan di halaman utama lainnya
                else -> {
                    binding.bottomNav.isVisible = true
                }
            }
        }
    }
}