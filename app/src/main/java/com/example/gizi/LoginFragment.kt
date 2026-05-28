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

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            if (isAdded) {
                Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

            // Disable button to prevent double-click
            binding.btnLogin.isEnabled = false

            // SELALU GUNAKAN FIREBASE AUTH UNTUK LOGIN
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                // Check if fragment is still attached to activity
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    // Berhasil login ke Cloud, sekarang aman untuk simpan data
                    PrefsHelper.clearData(requireContext())
                    // Only navigate if we are still at the login screen
                    if (findNavController().currentDestination?.id == R.id.navigation_login) {
                        findNavController().navigate(R.id.action_login_to_onboarding)
                    }
                } else {
                    binding.btnLogin.isEnabled = true
                    // Cek apakah ini akun lokal lama atau memang salah password
                    val sharedPref = requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
                    val registeredEmail = sharedPref.getString("user_email", null)
                    
                    if (email == registeredEmail) {
                        Toast.makeText(requireContext(), "Akun belum sinkron Cloud. Silakan Daftar Ulang agar UID aktif.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal masuk: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful) {
                val user = auth.currentUser
                val sharedPref = requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
                sharedPref.edit {
                    putString("user_name", user?.displayName)
                    putString("user_email", user?.email)
                }
                if (findNavController().currentDestination?.id == R.id.navigation_login) {
                    findNavController().navigate(R.id.action_login_to_onboarding)
                }
            } else {
                Toast.makeText(requireContext(), "Firebase Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
