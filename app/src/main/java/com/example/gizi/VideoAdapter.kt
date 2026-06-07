package com.example.gizi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gizi.databinding.ItemVideoBinding
import com.example.gizi.model.Video

class VideoAdapter : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            binding.tvVideoTitle.text = video.title
            binding.tvVideoLevel.text = video.level
            binding.tvVideoCategory.text = video.category


            if (!video.thumbnailUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(binding.root.context)
                    .load(video.thumbnailUrl)
                    .placeholder(android.R.color.darker_gray)
                    .centerCrop()
                    .into(binding.ivVideoThumbnail)
            }

            binding.btnWatch.setOnClickListener {
                openYoutube(it.context, video.youtubeUrl)
            }
            binding.flVideoThumbnail.setOnClickListener {
                openYoutube(it.context, video.youtubeUrl)
            }
        }

        private fun openYoutube(context: android.content.Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.setPackage("com.google.android.youtube")
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
}
