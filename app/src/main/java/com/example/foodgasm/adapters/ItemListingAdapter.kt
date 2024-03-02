package com.example.foodgasm.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgasm.data.Restaurant

import com.example.foodgasm.databinding.ParentItemBinding


class ItemListingAdapter : RecyclerView.Adapter<ItemListingAdapter.ItemListingViewHolder>() {
    inner class ItemListingViewHolder(val binding: ParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: Restaurant) {
            binding.apply {
                Log.d("ImageURL2", "Image URL: ${restaurant.image}")
                name.text = restaurant.name
            }
        }
    }

    private val diffUtil2 = object : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Restaurant,
            newItem: Restaurant
        ): Boolean {
            return oldItem == newItem
        }

    }

    val differ2 = AsyncListDiffer(this, diffUtil2)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListingViewHolder {
        val binding = ParentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemListingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ2.currentList.size
    }

    override fun onBindViewHolder(holder: ItemListingViewHolder, position: Int) {

        val restaurant = differ2.currentList[position]
        restaurant.let {
            holder.bind(it)
        }

        holder.binding.root.setOnClickListener {
            onClick?.invoke(restaurant)
        }
    }

    var onClick: ((Restaurant) -> Unit)? = null
}