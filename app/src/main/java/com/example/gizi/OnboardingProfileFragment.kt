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

class OnboardingProfileFragment : Fragment() {

    private var _binding: FragmentOnboardingProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Default pilih "Jaga Berat Badan"
        selectTujuan("jaga")

        // ✅ Klik card langsung pilih tujuan + ripple
        binding.cvTurun.setOnClickListener { selectTujuan("turun") }
        binding.cvJaga.setOnClickListener { selectTujuan("jaga") }
        binding.cvNaik.setOnClickListener { selectTujuan("naik") }

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

        binding.btnHitung.setOnClickListener { hitungKalori() }
        binding.btnMulai.setOnClickListener { simpanDanLanjut() }
    }

    private var tujuanDipilih = "jaga"

    private fun selectTujuan(pilihan: String) {
        tujuanDipilih = pilihan

        val green = resources.getColor(R.color.primary_green, null)
        val border = resources.getColor(R.color.card_border, null)

        binding.cvTurun.setStrokeColor(if (pilihan == "turun") green else border)
        binding.cvJaga.setStrokeColor(if (pilihan == "jaga") green else border)
        binding.cvNaik.setStrokeColor(if (pilihan == "naik") green else border)

        binding.rbTurun.isChecked = pilihan == "turun"
        binding.rbJaga.isChecked = pilihan == "jaga"
        binding.rbNaik.isChecked = pilihan == "naik"

        // Reset preview kalau ganti pilihan
        binding.cvPreviewKalori.isVisible = false
        binding.btnMulai.isVisible = false
    }

    private fun hitungKalori() {
        val berat = binding.etBerat.text.toString().toFloatOrNull()
        val tinggi = binding.etTinggi.text.toString().toFloatOrNull()

        if (berat == null || berat <= 0) {
            binding.etBerat.error = "Masukkan berat badan yang valid"
            return
        }
        if (tinggi == null || tinggi <= 0) {
            binding.etTinggi.error = "Masukkan tinggi badan yang valid"
            return
        }

        val tujuan = getTujuanDipilih()
        val targetKalori = PrefsHelper.hitungTargetKalori(berat, tinggi, tujuan)

        binding.tvPreviewKalori.text = targetKalori.toString()
        binding.tvPreviewDesc.text = when (tujuan) {
            "Turun Berat Badan" -> "Defisit 500 kkal dari kebutuhan harianmu"
            "Naik Berat Badan"  -> "Surplus 500 kkal dari kebutuhan harianmu"
            else                -> "Sesuai kebutuhan kalori harianmu"
        }
        binding.cvPreviewKalori.isVisible = true
        binding.btnMulai.isVisible = true
    }

    private fun simpanDanLanjut() {
        val berat = binding.etBerat.text.toString().toFloatOrNull() ?: return
        val tinggi = binding.etTinggi.text.toString().toFloatOrNull() ?: return
        val tujuan = getTujuanDipilih()

        PrefsHelper.simpanProfil(requireContext(), berat, tinggi, tujuan)

        // ✅ Simpan flag onboarding_done ke Firestore biar permanen
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("onboarding_done", true)
        }

        Toast.makeText(requireContext(), "Profil tersimpan!", Toast.LENGTH_SHORT).show()
        
        // Cek apakah action ini ada di nav_graph
        try {
            findNavController().navigate(R.id.action_onboarding_profile_to_onboarding_welcome)
        } catch (_: Exception) {
            findNavController().navigate(R.id.navigation_onboarding)
        }
    }

    private fun getTujuanDipilih(): String = when (tujuanDipilih) {
        "turun" -> "Turun Berat Badan"
        "naik"  -> "Naik Berat Badan"
        else    -> "Jaga Berat Badan"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
