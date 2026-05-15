package com.example.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.SecretSpot

class SecretSpotAdapter(private val spots: List<SecretSpot>) : RecyclerView.Adapter<SecretSpotAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSpotImage: ImageView = view.findViewById(R.id.ivSpotImage)
        val tvSpotName: TextView = view.findViewById(R.id.tvSpotName)
        val tvSpotType: TextView = view.findViewById(R.id.tvSpotType)
        val tvSpotDescription: TextView = view.findViewById(R.id.tvSpotDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_secret_spot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        holder.tvSpotName.text = spot.name
        holder.tvSpotType.text = spot.type
        holder.tvSpotDescription.text = spot.description

        if (spot.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(spot.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivSpotImage)
        }
    }

    override fun getItemCount() = spots.size
}
