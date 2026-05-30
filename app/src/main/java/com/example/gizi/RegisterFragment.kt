package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentRegisterBinding
import com.example.gizi.helper.PrefsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnRegister.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Reset errors
            binding.tilPassword.error = null
            binding.tilConfirmPassword.error = null

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(requireContext(), "Email harus menggunakan @gmail.com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Password tidak cocok"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.tilPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            // Menampilkan loading state
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Memproses..."

            // --- PROSES REGISTRASI KE FIREBASE ---
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    
                    // 1. Simpan Profil ke Firestore
                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    db.collection("users").document(userId).set(userProfile)
                        .addOnSuccessListener {
                            if (!isAdded) return@addOnSuccessListener

                            // ✅ clearData dulu, lalu simpan ke GIZI_PREFS
                            PrefsHelper.clearData(requireContext())
                            requireActivity()
                                .getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
                                .edit {
                                    putString("user_name", name)
                                    putString("user_email", email)
                                }

                            Toast.makeText(requireContext(), "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                            if (findNavController().currentDestination?.id == R.id.navigation_register) {
                                findNavController().navigate(R.id.action_register_to_login)
                            }
                        }
                        .addOnFailureListener { e ->
                            if (!isAdded) return@addOnFailureListener
                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.text = "Daftar Sekarang"
                            Toast.makeText(requireContext(), "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar Sekarang"
                    
                    val exception = task.exception
                    val errorMessage = when {
                        exception is FirebaseAuthUserCollisionException -> "Email sudah terdaftar. Gunakan email lain."
                        exception?.message?.contains("configuration") == true -> "Error: Email/Password belum diaktifkan di Firebase Console."
                        exception?.message?.contains("network") == true -> "Error: Tidak ada koneksi internet."
                        else -> "Firebase Auth Error: ${exception?.localizedMessage}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
