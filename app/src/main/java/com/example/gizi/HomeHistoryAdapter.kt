package com.example.gizi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gizi.databinding.ItemFoodSelectedBinding
import com.example.gizi.model.FoodItem
import java.util.Locale

class HomeHistoryAdapter : ListAdapter<FoodItem, HomeHistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemFoodSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodItem) {
            binding.tvNamaMakananSelected.text = food.description
            binding.tvKaloriSelected.text = String.format(Locale.getDefault(), "%.0f kal", food.getKalori())
            binding.btnHapus.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodSelectedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem) =
            oldItem.fdcId == newItem.fdcId
        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem) =
            oldItem == newItem
    }
}
