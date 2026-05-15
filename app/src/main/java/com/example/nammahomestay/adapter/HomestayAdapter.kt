package com.example.nammahomestay.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.Homestay
import com.example.nammahomestay.ui.host.EditHomestayActivity
import com.example.nammahomestay.ui.host.EditMenuActivity
import com.google.firebase.auth.FirebaseAuth

class HomestayAdapter(
    private val homestays: List<Homestay>,
    private val onItemClick: (Homestay) -> Unit
) : RecyclerView.Adapter<HomestayAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivHomestayImage)
        val tvName: TextView = view.findViewById(R.id.tvHomestayName)
        val tvLocation: TextView = view.findViewById(R.id.tvHomestayLocation)
        val tvPrice: TextView = view.findViewById(R.id.tvHomestayPrice)
        val tvDescription: TextView = view.findViewById(R.id.tvHomestayDescription)
        val tvSpots: TextView = view.findViewById(R.id.tvHomestaySpots)
        val btnUpdateMenu: Button = view.findViewById(R.id.btnUpdateMenu)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditHomestay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_homestay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homestay = homestays[position]
        val currentUser = FirebaseAuth.getInstance().currentUser

        holder.tvName.text = homestay.name
        holder.tvLocation.text = homestay.location
        holder.tvPrice.text = "₹${homestay.price} per night"
        holder.tvDescription.text = homestay.description
        
        if (homestay.nearbySpots.isNotEmpty()) {
            val spotsText = homestay.nearbySpots.joinToString(", ") { it.name }
            holder.tvSpots.text = spotsText
        } else {
            holder.tvSpots.text = "No spots researched yet."
        }

        if (homestay.imageUrls.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(homestay.imageUrls[0])
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivImage)
        }

        // Host specific actions
        if (currentUser != null && currentUser.uid == homestay.hostId) {
            holder.btnUpdateMenu.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.VISIBLE
        } else {
            holder.btnUpdateMenu.visibility = View.GONE
            holder.btnEdit.visibility = View.GONE
        }

        holder.btnUpdateMenu.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditMenuActivity::class.java).apply {
                putExtra("HOMESTAY_ID", homestay.id)
                putExtra("HOMESTAY_NAME", homestay.name)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditHomestayActivity::class.java).apply {
                putExtra("HOMESTAY_ID", homestay.id)
                putExtra("NAME", homestay.name)
                putExtra("LOCATION", homestay.location)
                putExtra("PRICE", homestay.price)
                putExtra("DESCRIPTION", homestay.description)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            onItemClick(homestay)
        }
    }

    override fun getItemCount() = homestays.size
}
