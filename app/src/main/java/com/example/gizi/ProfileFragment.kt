package com.example.gizi

import android.content.Context
import androidx.core.net.toUri
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentProfileBinding
import com.example.gizi.helper.PrefsHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Simpan URI dan tampilkan foto
            PrefsHelper.simpanProfil(
                requireContext(),
                PrefsHelper.getBeratBadan(requireContext()),
                PrefsHelper.getTinggiBadan(requireContext()),
                PrefsHelper.getTujuan(requireContext()),
                it.toString(),
                PrefsHelper.getUsia(requireContext()),
                PrefsHelper.getJenisKelamin(requireContext())
            )
            tampilkanFoto(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        muatDataProfil()
        setupListeners()
    }

    private fun muatDataProfil() {
        if (!isAdded || _binding == null) return
        val prefs = requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
        val name = prefs.getString("user_name", "User") ?: "User"
        val email = prefs.getString("user_email", "") ?: ""

        binding.tvUserNameProfile.text = name
        binding.tvUserEmailProfile.text = email
        binding.tvEmailValue.text = email
        binding.tvInitial.text = name.firstOrNull()?.uppercase() ?: "G"

        val berat = PrefsHelper.getBeratBadan(requireContext())
        val tinggi = PrefsHelper.getTinggiBadan(requireContext())
        val tujuan = PrefsHelper.getTujuan(requireContext())
        val target = PrefsHelper.getTargetKalori(requireContext())
        
        // Bonus: Kita bisa tampilkan usia dan gender di log atau UI jika ada
        val usia = PrefsHelper.getUsia(requireContext())
        val gender = PrefsHelper.getJenisKelamin(requireContext())

        binding.tvBeratValue.text = if (berat > 0) "${berat.toInt()} kg" else "-- kg"
        binding.tvTinggiValue.text = if (tinggi > 0) "${tinggi.toInt()} cm" else "-- cm"
        binding.tvTargetValue.text = if (target > 0) "$target kkal" else "-- kkal"
        binding.tvTujuanValue.text = tujuan
        binding.tvGenderUsia.text = "$gender · $usia tahun"

        // Tampilkan foto kalau ada
        val fotoUri = PrefsHelper.getFotoProfil(requireContext())
        tampilkanFoto(fotoUri)
    }

    private fun tampilkanFoto(uriString: String) {
        if (_binding == null) return
        if (uriString.isNotEmpty()) {
            try {
                val uri = uriString.toUri()
                binding.ivFotoProfil.setImageURI(uri)
                binding.ivFotoProfil.isVisible = true
                binding.vInitialBg.isVisible = false
                binding.tvInitial.isVisible = false
            } catch (_: Exception) {
                binding.ivFotoProfil.isVisible = false
                binding.vInitialBg.isVisible = true
                binding.tvInitial.isVisible = true
            }
        } else {
            binding.ivFotoProfil.isVisible = false
            binding.vInitialBg.isVisible = true
            binding.tvInitial.isVisible = true
        }
    }

    private fun setupListeners() {
        binding.cvEditFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvEditProfil.setOnClickListener {
            showEditDialog()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            PrefsHelper.clearAllData(requireContext())
            findNavController().navigate(R.id.navigation_login)
        }
    }

    private fun showEditDialog() {
        if (!isAdded) return
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profil, null)

        val etBerat = dialogView.findViewById<TextInputEditText>(R.id.et_edit_berat)
        val etTinggi = dialogView.findViewById<TextInputEditText>(R.id.et_edit_tinggi)
        val etUsia = dialogView.findViewById<TextInputEditText>(R.id.et_edit_usia)
        val rbLaki = dialogView.findViewById<RadioButton>(R.id.rb_edit_laki)
        val rbPerempuan = dialogView.findViewById<RadioButton>(R.id.rb_edit_perempuan)
        
        val rbTurun = dialogView.findViewById<RadioButton>(R.id.rb_edit_turun)
        val rbJaga = dialogView.findViewById<RadioButton>(R.id.rb_edit_jaga)
        val rbNaik = dialogView.findViewById<RadioButton>(R.id.rb_edit_naik)

        // Isi dengan data saat ini
        val beratSaatIni = PrefsHelper.getBeratBadan(requireContext())
        val tinggiSaatIni = PrefsHelper.getTinggiBadan(requireContext())
        val usiaSaatIni = PrefsHelper.getUsia(requireContext())
        val genderSaatIni = PrefsHelper.getJenisKelamin(requireContext())
        val tujuanSaatIni = PrefsHelper.getTujuan(requireContext())

        if (beratSaatIni > 0) etBerat.setText(beratSaatIni.toInt().toString())
        if (tinggiSaatIni > 0) etTinggi.setText(tinggiSaatIni.toInt().toString())
        etUsia.setText(usiaSaatIni.toString())
        
        if (genderSaatIni == "Laki-laki") rbLaki.isChecked = true else rbPerempuan.isChecked = true

        when (tujuanSaatIni) {
            "Turun Berat Badan" -> rbTurun.isChecked = true
            "Naik Berat Badan"  -> rbNaik.isChecked = true
            else                -> rbJaga.isChecked = true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Data Tubuh")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val berat = etBerat.text.toString().toFloatOrNull()
                val tinggi = etTinggi.text.toString().toFloatOrNull()
                val usia = etUsia.text.toString().toIntOrNull() ?: usiaSaatIni
                val gender = if (rbLaki.isChecked) "Laki-laki" else "Perempuan"
                
                val tujuan = when {
                    rbTurun.isChecked -> "Turun Berat Badan"
                    rbNaik.isChecked  -> "Naik Berat Badan"
                    else              -> "Jaga Berat Badan"
                }

                if (berat == null || tinggi == null) {
                    Toast.makeText(requireContext(), "Data tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val fotoUri = PrefsHelper.getFotoProfil(requireContext())
                PrefsHelper.simpanProfil(requireContext(), berat, tinggi, tujuan, fotoUri, usia, gender)
                muatDataProfil()
                Toast.makeText(requireContext(), "Profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
