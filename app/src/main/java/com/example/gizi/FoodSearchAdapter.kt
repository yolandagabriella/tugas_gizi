package com.example.gizi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gizi.databinding.ItemFoodSearchBinding
import com.example.gizi.model.FoodItem
import java.util.Locale

class FoodSearchAdapter(
    private val onAddClick: (FoodItem) -> Unit,
) : ListAdapter<FoodItem, FoodSearchAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemFoodSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodItem) {
            binding.tvNamaMakanan.text = food.description
            binding.tvNutrisiInfo.text = String.format(
                Locale.getDefault(),
                "%.0f kal | P: %.1fg | C: %.1fg | F: %.1fg",
                food.getKalori(),
                food.getProtein(),
                food.getKarbohidrat(),
                food.getLemak(),
            )
            binding.btnTambah.setOnClickListener { onAddClick(food) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
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
