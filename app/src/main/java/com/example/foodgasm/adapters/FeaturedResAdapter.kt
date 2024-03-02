package com.example.foodgasm.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgasm.data.Restaurant
import com.example.foodgasm.databinding.RestaurantRvItemBinding
import com.squareup.picasso.Picasso

class FeaturedResAdapter(
    private val smallItem: Boolean = false,
    private val onClick: ((Restaurant) -> Unit)? = null
) : RecyclerView.Adapter<FeaturedResAdapter.FeaturedResViewHolder>() {
    inner class FeaturedResViewHolder(val binding: RestaurantRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            if (smallItem) {
                binding.featuredtext.visibility = View.GONE
                binding.llFavorite.visibility = View.GONE
            }
        }

        fun bind(restaurant: Restaurant) {
            binding.apply {
                Log.d("Glide", "Loading image: ${restaurant.image}")
                Picasso.get().load(restaurant.image).into(img)
                tvAdName.text = restaurant.name
                address.text = restaurant.address
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: Restaurant,
            newItem: Restaurant
        ): Boolean {
            return (oldItem == newItem)
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedResViewHolder {
        return FeaturedResViewHolder(
            RestaurantRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            itemView.setOnClickListener {
                onClick?.invoke(differ.currentList[bindingAdapterPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: FeaturedResViewHolder, position: Int) {
        val restaurant = differ.currentList[position]
        holder.bind(restaurant)
    }
}