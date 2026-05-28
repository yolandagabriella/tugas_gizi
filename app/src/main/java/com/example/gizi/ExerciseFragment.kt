package com.example.gizi

import android.content.Intent
import androidx.core.net.toUri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gizi.databinding.FragmentExerciseBinding

class ExerciseFragment : Fragment() {
    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val link1 = "https://youtu.be/W2sZpyfY7SU"
        val link2 = "https://youtu.be/ow3hpYJqYEI"

        binding.thumbnailVideo1.setOnClickListener { openYoutube(link1) }
        binding.btnTontonVideo1.setOnClickListener { openYoutube(link1) }

        binding.thumbnailVideo2.setOnClickListener { openYoutube(link2) }
        binding.btnTontonVideo2.setOnClickListener { openYoutube(link2) }
    }

    private fun openYoutube(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.setPackage("com.google.android.youtube")
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}