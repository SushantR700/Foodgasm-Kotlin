package com.example.foodgasm.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgasm.data.Restaurant
import com.example.foodgasm.databinding.CategoryRvItemBinding
import com.squareup.picasso.Picasso

class HealthyFoodAdapter:RecyclerView.Adapter<HealthyFoodAdapter.HealthyFoodViewHolder>() {

    inner class HealthyFoodViewHolder(val binding:CategoryRvItemBinding ):RecyclerView.ViewHolder(binding.root){

        fun bind(restaurant:Restaurant){
            binding.apply {
                Log.d("Glide", "Loading image: ${restaurant.image}")
                Picasso.get().load(restaurant.image).into(img)
                tvAdName.text=restaurant.name.toString()
                address.text=restaurant.address.toString()

            }
        }
    }

    val diffUtil = object : ItemCallback<Restaurant>(){
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
          return ( oldItem.id ==newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: Restaurant,
            newItem: Restaurant
        ): Boolean {
            return (oldItem==newItem)
        }

    }

    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthyFoodViewHolder {
        return HealthyFoodViewHolder(
            CategoryRvItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: HealthyFoodViewHolder, position: Int) {
        val restaurant = differ.currentList[position]
        holder.bind(restaurant)
    }
}