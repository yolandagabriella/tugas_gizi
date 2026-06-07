package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
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
                    val user = auth.currentUser ?: run {
                        binding.btnLogin.isEnabled = true
                        return@addOnCompleteListener
                    }

                    // ✅ Ambil nama dari Firestore, simpan ke GIZI_PREFS
                    db.collection("users").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (!isAdded) return@addOnSuccessListener
                            val name = document.getString("name")
                                ?: user.displayName
                                ?: "User"
                            val userEmail = document.getString("email")
                                ?: user.email
                                ?: ""
                            saveUserAndNavigate(name, userEmail)
                        }
                        .addOnFailureListener {
                            if (!isAdded) return@addOnFailureListener
                            // Fallback ke data Auth kalau Firestore gagal
                            saveUserAndNavigate(
                                user.displayName ?: "User",
                                user.email ?: ""
                            )
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

    private fun saveUserAndNavigate(name: String, email: String) {
        PrefsHelper.clearData(requireContext())

        requireActivity()
            .getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
            .edit {
                putString("user_name", name)
                putString("user_email", email)
            }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            navigateToHome()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                val onboardingDone = document.getBoolean("onboarding_done")

                when {
                    // onboarding_done = true → user lama → beranda
                    onboardingDone == true -> navigateToHome()

                    // onboarding_done = false → user baru yang baru daftar → isi data tubuh
                    onboardingDone == false -> {
                        if (findNavController().currentDestination?.id == R.id.navigation_login) {
                            findNavController().navigate(R.id.action_login_to_onboarding_profile)
                        }
                    }

                    // onboarding_done = null → user lama yang dokumennya belum ada field ini
                    // → langsung set true dan ke beranda
                    else -> {
                        db.collection("users").document(uid)
                            .update("onboarding_done", true)
                        navigateToHome()
                    }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                navigateToHome()
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
                saveUserAndNavigate(
                    user?.displayName ?: "User",
                    user?.email ?: ""
                )
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
