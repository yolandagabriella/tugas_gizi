package com.example.gizi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentOnboardingProfileBinding
import com.example.gizi.helper.PrefsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class OnboardingProfileFragment : Fragment() {

    private var _binding: FragmentOnboardingProfileBinding? = null
    private val binding get() = _binding!!

    private var jenisKelaminDipilih = "Perempuan"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectJenisKelamin("Perempuan")

        binding.cvLaki.setOnClickListener { selectJenisKelamin("Laki-laki") }
        binding.cvPerempuan.setOnClickListener { selectJenisKelamin("Perempuan") }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.cvPreviewKalori.isVisible = false
                binding.btnMulai.isVisible = false
            }
        }
        binding.etBerat.addTextChangedListener(watcher)
        binding.etTinggi.addTextChangedListener(watcher)
        binding.etUsia.addTextChangedListener(watcher)
        binding.etTargetBerat.addTextChangedListener(watcher)

        binding.btnHitung.setOnClickListener { hitungKalori() }
        binding.btnMulai.setOnClickListener { simpanDanLanjut() }
    }

    private fun selectJenisKelamin(pilihan: String) {
        jenisKelaminDipilih = pilihan
        val green = resources.getColor(R.color.primary_green, null)
        val border = resources.getColor(R.color.card_border, null)

        binding.cvLaki.setStrokeColor(android.content.res.ColorStateList.valueOf(if (pilihan == "Laki-laki") green else border))
        binding.rbLaki.isChecked = (pilihan == "Laki-laki")
        binding.cvPerempuan.setStrokeColor(android.content.res.ColorStateList.valueOf(if (pilihan == "Perempuan") green else border))
        binding.rbPerempuan.isChecked = (pilihan == "Perempuan")

        binding.cvPreviewKalori.isVisible = false
        binding.btnMulai.isVisible = false
    }

    private fun hitungKalori() {
        val berat = binding.etBerat.text.toString().toFloatOrNull()
        val tinggi = binding.etTinggi.text.toString().toFloatOrNull()
        val usia = binding.etUsia.text.toString().toIntOrNull()
        val targetBerat = binding.etTargetBerat.text.toString().toFloatOrNull()

        if (berat == null || berat <= 0) {
            binding.etBerat.error = "Masukkan berat badan yang valid"
            return
        }
        if (tinggi == null || tinggi <= 0) {
            binding.etTinggi.error = "Masukkan tinggi badan yang valid"
            return
        }
        if (usia == null || usia <= 0) {
            binding.etUsia.error = "Masukkan usia yang valid"
            return
        }
        if (targetBerat == null || targetBerat <= 0) {
            binding.etTargetBerat.error = "Masukkan target berat badan"
            return
        }

        val targetKalori = PrefsHelper.hitungTargetKaloriDariTargetBerat(
            berat, targetBerat, tinggi, usia, jenisKelaminDipilih
        )

        val tujuan = when {
            berat > targetBerat + 1f -> "Turun Berat Badan"
            berat < targetBerat - 1f -> "Naik Berat Badan"
            else                     -> "Jaga Berat Badan"
        }

        val selisih = kotlin.math.abs(berat - targetBerat)
        val locale = java.util.Locale.getDefault()

        binding.tvPreviewTujuan.text = when (tujuan) {
            "Turun Berat Badan" -> "Turunkan ${String.format(locale, "%.1f", selisih)} kg → $tujuan"
            "Naik Berat Badan"  -> "Naikkan ${String.format(locale, "%.1f", selisih)} kg → $tujuan"
            else                -> "Berat sudah ideal → $tujuan"
        }
        binding.tvPreviewKalori.text = targetKalori.toString()
        binding.tvPreviewDesc.text = when (tujuan) {
            "Turun Berat Badan" -> "Defisit 500 kkal/hari dari kebutuhan harianmu"
            "Naik Berat Badan"  -> "Surplus 500 kkal/hari dari kebutuhan harianmu"
            else                -> "Sesuai kebutuhan kalori harianmu"
        }

        binding.cvPreviewKalori.isVisible = true
        binding.btnMulai.isVisible = true
    }

    private fun simpanDanLanjut() {
        val berat = binding.etBerat.text.toString().toFloatOrNull() ?: return
        val tinggi = binding.etTinggi.text.toString().toFloatOrNull() ?: return
        val usia = binding.etUsia.text.toString().toIntOrNull() ?: 25
        val targetBerat = binding.etTargetBerat.text.toString().toFloatOrNull() ?: berat

        val tujuan = when {
            berat > targetBerat + 1f -> "Turun Berat Badan"
            berat < targetBerat - 1f -> "Naik Berat Badan"
            else                     -> "Jaga Berat Badan"
        }

        // 1. Simpan Lokal
        PrefsHelper.simpanProfil(
            requireContext(), berat, tinggi, tujuan,
            PrefsHelper.getFotoProfil(requireContext()),
            usia, jenisKelaminDipilih, targetBerat
        )

        // 2. Simpan ke Firestore agar tidak hilang saat login ulang
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val profileData = hashMapOf(
                "berat_badan" to berat,
                "tinggi_badan" to tinggi,
                "tujuan_kesehatan" to tujuan,
                "usia" to usia,
                "jenis_kelamin" to jenisKelaminDipilih,
                "target_berat" to targetBerat,
                "onboarding_done" to true
            )

            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .set(profileData, SetOptions.merge())
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Profil tersimpan!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_onboarding_profile_to_onboarding_welcome)
                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Gagal simpan ke cloud: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Tetap lanjut ke welcome walau cloud gagal (agar user tidak terjebak)
                        findNavController().navigate(R.id.action_onboarding_profile_to_onboarding_welcome)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
