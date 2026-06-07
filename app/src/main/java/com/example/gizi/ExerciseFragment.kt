package com.example.gizi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.databinding.FragmentExerciseBinding
import com.example.gizi.viewmodel.ExerciseViewModel
import com.google.android.material.chip.Chip

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var videoAdapter: VideoAdapter

    private val levels = listOf("Pemula", "Menengah", "Mahir")
    private val categories = listOf("Yoga", "Cardio", "Strength", "HIIT", "Stretching", "Pilates")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter()
        binding.rvVideos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
        }
    }

    private fun setupChips() {
        binding.llLevelChips.removeAllViews()
        binding.llKategoriChips.removeAllViews()

        levels.forEach { level ->
            binding.llLevelChips.addView(
                buatChip(level, level == viewModel.selectedLevel) {
                    viewModel.selectedLevel = level
                    refreshChips()
                    videoAdapter = VideoAdapter()
                    binding.rvVideos.adapter = videoAdapter
                    viewModel.fetchVideos()
                }
            )
        }

        categories.forEach { kategori ->
            binding.llKategoriChips.addView(
                buatChip(kategori, kategori == viewModel.selectedKategori) {
                    viewModel.selectedKategori = kategori
                    refreshChips()
                    viewModel.fetchVideos()
                }
            )
        }
    }

    private fun buatChip(text: String, isSelected: Boolean, onClick: () -> Unit): Chip {
        val green = resources.getColor(R.color.primary_green, null)
        val white = resources.getColor(R.color.white, null)
        val grayBorder = resources.getColor(R.color.card_border, null)
        val textDark = resources.getColor(R.color.text_dark, null)

        return Chip(requireContext()).apply {
            this.text = text
            textSize = 12f
            isCheckable = false
            isClickable = true
            isFocusable = true
            chipMinHeight = 72f

            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, 12, 0)
            layoutParams = lp

            if (isSelected) {
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(green)
                setTextColor(white)
                chipStrokeWidth = 0f
            } else {
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(white)
                setTextColor(textDark)
                chipStrokeColor = android.content.res.ColorStateList.valueOf(grayBorder)
                chipStrokeWidth = 2f
            }

            setOnClickListener { onClick() }
        }
    }

    private fun refreshChips() {
        val green = resources.getColor(R.color.primary_green, null)
        val white = resources.getColor(R.color.white, null)
        val grayBorder = resources.getColor(R.color.card_border, null)
        val textDark = resources.getColor(R.color.text_dark, null)

        for (i in 0 until binding.llLevelChips.childCount) {
            val chip = binding.llLevelChips.getChildAt(i) as? Chip ?: continue
            val selected = chip.text == viewModel.selectedLevel
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                if (selected) green else white
            )
            chip.setTextColor(if (selected) white else textDark)
            chip.chipStrokeWidth = if (selected) 0f else 2f
            if (!selected) chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(grayBorder)
        }

        for (i in 0 until binding.llKategoriChips.childCount) {
            val chip = binding.llKategoriChips.getChildAt(i) as? Chip ?: continue
            val selected = chip.text == viewModel.selectedKategori
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                if (selected) green else white
            )
            chip.setTextColor(if (selected) white else textDark)
            chip.chipStrokeWidth = if (selected) 0f else 2f
            if (!selected) chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(grayBorder)
        }
    }

    private fun observeViewModel() {
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            videoAdapter.submitList(videos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.rvVideos.isVisible = !loading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
