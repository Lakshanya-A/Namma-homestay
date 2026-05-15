package com.example.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.Dish

class CustomerDishAdapter(private val dishes: List<Dish>) : RecyclerView.Adapter<CustomerDishAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivDishImage: ImageView = view.findViewById(R.id.ivDishImage)
        val tvDishName: TextView = view.findViewById(R.id.tvDishName)
        val tvDishDescription: TextView = view.findViewById(R.id.tvDishDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        // Hide edit/delete buttons for customers if they exist in the layout
        view.findViewById<View>(R.id.llActions)?.visibility = View.GONE
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishes[position]
        holder.tvDishName.text = dish.dishName
        holder.tvDishDescription.text = dish.description

        if (dish.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(dish.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivDishImage)
        }

        // Show full description in a dialog when tapped
        holder.itemView.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle(dish.dishName)
                .setMessage(dish.description)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    override fun getItemCount() = dishes.size
}
