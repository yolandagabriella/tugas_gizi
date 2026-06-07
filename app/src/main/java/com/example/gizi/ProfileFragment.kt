package com.example.gizi

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val internalUri = copyImageToInternalStorage(it)
            if (internalUri != null) {
                // Simpan URI dan tampilkan foto
                PrefsHelper.simpanProfil(
                    requireContext(),
                    PrefsHelper.getBeratBadan(requireContext()),
                    PrefsHelper.getTinggiBadan(requireContext()),
                    PrefsHelper.getTujuan(requireContext()),
                    internalUri,
                    PrefsHelper.getUsia(requireContext()),
                    PrefsHelper.getJenisKelamin(requireContext()),
                    PrefsHelper.getTargetBerat(requireContext())
                )
                tampilkanFoto(internalUri)
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        val targetBerat = PrefsHelper.getTargetBerat(requireContext())
        
        // Bonus: Kita bisa tampilkan usia dan gender di log atau UI jika ada
        val usia = PrefsHelper.getUsia(requireContext())
        val gender = PrefsHelper.getJenisKelamin(requireContext())

        binding.tvBeratValue.text = if (berat > 0) "${berat.toInt()} kg" else "-- kg"
        binding.tvTinggiValue.text = if (tinggi > 0) "${tinggi.toInt()} cm" else "-- cm"
        binding.tvTargetBeratValue.text = if (targetBerat > 0) "${targetBerat.toInt()} kg" else "-- kg"
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
            binding.ivFotoProfil.isVisible = true
            binding.vInitialBg.isVisible = false
            binding.tvInitial.isVisible = false

            Glide.with(this)
                .load(if (uriString.startsWith("/")) File(uriString) else uriString)
                .error(R.color.primary_green)
                .into(binding.ivFotoProfil)
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
        val etTargetBerat = dialogView.findViewById<TextInputEditText>(R.id.et_edit_target_berat)
        val etUsia = dialogView.findViewById<TextInputEditText>(R.id.et_edit_usia)
        val rbLaki = dialogView.findViewById<RadioButton>(R.id.rb_edit_laki)
        val rbPerempuan = dialogView.findViewById<RadioButton>(R.id.rb_edit_perempuan)
        
        val rbTurun = dialogView.findViewById<RadioButton>(R.id.rb_edit_turun)
        val rbJaga = dialogView.findViewById<RadioButton>(R.id.rb_edit_jaga)
        val rbNaik = dialogView.findViewById<RadioButton>(R.id.rb_edit_naik)

        // Isi dengan data saat ini
        val beratSaatIni = PrefsHelper.getBeratBadan(requireContext())
        val tinggiSaatIni = PrefsHelper.getTinggiBadan(requireContext())
        val targetBeratSaatIni = PrefsHelper.getTargetBerat(requireContext())
        val usiaSaatIni = PrefsHelper.getUsia(requireContext())
        val genderSaatIni = PrefsHelper.getJenisKelamin(requireContext())
        val tujuanSaatIni = PrefsHelper.getTujuan(requireContext())

        if (beratSaatIni > 0) etBerat.setText(beratSaatIni.toInt().toString())
        if (tinggiSaatIni > 0) etTinggi.setText(tinggiSaatIni.toInt().toString())
        if (targetBeratSaatIni > 0) etTargetBerat.setText(targetBeratSaatIni.toInt().toString())
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
                val targetBerat = etTargetBerat.text.toString().toFloatOrNull() ?: beratSaatIni
                val usia = etUsia.text.toString().toIntOrNull() ?: usiaSaatIni
                val gender = if (rbLaki.isChecked) "Laki-laki" else "Perempuan"
                
                if (berat == null || tinggi == null) {
                    Toast.makeText(requireContext(), "Data tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val tujuan = when {
                    berat > targetBerat + 1f -> "Turun Berat Badan"
                    berat < targetBerat - 1f   -> "Naik Berat Badan"
                    else                       -> "Jaga Berat Badan"
                }

                val fotoUri = PrefsHelper.getFotoProfil(requireContext())
                PrefsHelper.simpanProfil(requireContext(), berat, tinggi, tujuan, fotoUri, usia, gender, targetBerat)
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
