package com.example.gizi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gizi.databinding.FragmentOnboardingBinding

import androidx.appcompat.app.AlertDialog

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_onboarding_to_home)
        }

        binding.layoutNutrisi.setOnClickListener {
            showInfoDialog(getString(R.string.info_nutrisi_title), getString(R.string.info_nutrisi_desc))
        }

        binding.layoutOlahraga.setOnClickListener {
            showInfoDialog(getString(R.string.info_olahraga_title), getString(R.string.info_olahraga_desc))
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.oke_siap, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}