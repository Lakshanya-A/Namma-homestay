package com.example.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.NearbySpot

class NearbySpotAdapter(private val spots: List<NearbySpot>) : RecyclerView.Adapter<NearbySpotAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvSpotName)
        val tvType: TextView = view.findViewById(R.id.tvSpotType)
        val tvDescription: TextView = view.findViewById(R.id.tvSpotDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nearby_spot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        holder.tvName.text = spot.name
        holder.tvType.text = spot.type
        holder.tvDescription.text = spot.description
    }

    override fun getItemCount() = spots.size
}
