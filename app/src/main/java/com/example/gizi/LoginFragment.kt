package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentLoginBinding
import com.example.gizi.helper.PrefsHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Cek auto-login (Tetap Login jika aplikasi cuma ditutup)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkOnboardingAndNavigate(currentUser.uid)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Harap masukkan email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        fetchUserDataAndNavigate(user.uid, user.displayName, user.email)
                    }
                } else {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Gagal masuk: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnGoogleSignIn.setOnClickListener { signInWithGoogle() }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun fetchUserDataAndNavigate(uid: String, fallbackName: String?, fallbackEmail: String?) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                
                val name = document.getString("name") ?: fallbackName ?: "User"
                val email = document.getString("email") ?: fallbackEmail ?: ""
                
                // ✅ Tarik data tubuh dari Firestore agar tidak hilang saat login ulang
                val berat = document.getDouble("berat_badan")?.toFloat() ?: 0f
                val tinggi = document.getDouble("tinggi_badan")?.toFloat() ?: 0f
                val tujuan = document.getString("tujuan_kesehatan") ?: "Jaga Berat Badan"
                val usia = document.getLong("usia")?.toInt() ?: 25
                val gender = document.getString("jenis_kelamin") ?: "Perempuan"
                val targetBerat = document.getDouble("target_berat")?.toFloat() ?: 0f

                // ✅ Simpan semua ke Prefs lokal
                if (berat > 0f) {
                    PrefsHelper.simpanProfil(requireContext(), berat, tinggi, tujuan, "", usia, gender, targetBerat)
                }

                requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE).edit().apply {
                    putString("user_name", name)
                    putString("user_email", email)
                    apply()
                }

                val onboardingDone = document.getBoolean("onboarding_done")
                handleNavigation(onboardingDone, uid)
            }
            .addOnFailureListener {
                if (isAdded) {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal mengambil data profil.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkOnboardingAndNavigate(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                
                // Pastikan data lokal terisi walau auto-login
                val name = document.getString("name") ?: "User"
                val email = document.getString("email") ?: ""
                val berat = document.getDouble("berat_badan")?.toFloat() ?: 0f
                val tinggi = document.getDouble("tinggi_badan")?.toFloat() ?: 0f
                val tujuan = document.getString("tujuan_kesehatan") ?: "Jaga Berat Badan"
                val usia = document.getLong("usia")?.toInt() ?: 25
                val gender = document.getString("jenis_kelamin") ?: "Perempuan"
                val targetBerat = document.getDouble("target_berat")?.toFloat() ?: 0f

                if (berat > 0f) {
                    PrefsHelper.simpanProfil(requireContext(), berat, tinggi, tujuan, "", usia, gender, targetBerat)
                }

                requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE).edit().apply {
                    putString("user_name", name)
                    putString("user_email", email)
                    apply()
                }

                val onboardingDone = document.getBoolean("onboarding_done")
                handleNavigation(onboardingDone, uid)
            }
            .addOnFailureListener {
                if (isAdded) navigateToHome()
            }
    }

    private fun handleNavigation(onboardingDone: Boolean?, uid: String) {
        if (!isAdded) return
        
        when (onboardingDone) {
            true -> navigateToHome()
            false -> {
                // User baru -> harus isi data tubuh
                if (findNavController().currentDestination?.id == R.id.navigation_login) {
                    findNavController().navigate(R.id.action_login_to_onboarding_profile)
                }
            }
            else -> {
                // User lama atau field belum ada -> anggap sudah pernah isi data tubuh (biar ga pusing)
                db.collection("users").document(uid).update("onboarding_done", true)
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        if (!isAdded) return
        if (findNavController().currentDestination?.id == R.id.navigation_login) {
            findNavController().navigate(R.id.action_login_to_home)
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (!isAdded) return@addOnCompleteListener
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    fetchUserDataAndNavigate(user.uid, user.displayName, user.email)
                }
            } else {
                Toast.makeText(requireContext(), "Google login gagal.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
