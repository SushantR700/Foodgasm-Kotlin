package com.example.foodgasm.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgasm.data.Restaurant
import com.example.foodgasm.databinding.FeatureRvItemBinding
import com.squareup.picasso.Picasso

class NearByResAdapter() :RecyclerView.Adapter<NearByResAdapter.NearByResViewHolder>() {

    inner class NearByResViewHolder( val binding: FeatureRvItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(restaurant: Restaurant){
            binding.apply {
                Log.d("Glide", "Loading image: ${restaurant.image}")
                Picasso.get().load(restaurant.image).into(imgAd)
                tvAdName.text=restaurant.name.toString()
                address.text=restaurant.address.toString()
                duration.text=restaurant.duration.toString()
                discount.text=restaurant.discount.toString()

            }
        }
    }

    val diffUtil2 = object: ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Restaurant,
            newItem: Restaurant
        ): Boolean {
            return oldItem==newItem
        }

    }

    val differ2 =AsyncListDiffer(this,diffUtil2)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearByResViewHolder {
        val binding= FeatureRvItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NearByResViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ2.currentList.size
    }

    override fun onBindViewHolder(holder: NearByResViewHolder, position: Int) {
        val restaurant = differ2.currentList[position]
        restaurant.let {
            holder.bind(it)
        }
        holder.itemView.setOnClickListener {
            onClick?.invoke(restaurant)
        }
    }

    var onClick : ((Restaurant) -> Unit) ?=null
}