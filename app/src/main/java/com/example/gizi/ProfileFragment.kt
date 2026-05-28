package com.example.gizi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentProfileBinding
import com.example.gizi.helper.PrefsHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ambil data dari SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("GIZI_PREFS", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User")
        val email = sharedPref.getString("user_email", "user@email.com")

        // Tampilkan data di UI
        binding.tvUserNameProfile.text = name
        binding.tvUserEmailProfile.text = email
        binding.tvEmailValue.text = email
        binding.tvInitial.text = name?.firstOrNull()?.toString()?.uppercase() ?: "G"

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            PrefsHelper.clearData(requireContext())
            findNavController().navigate(R.id.navigation_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}