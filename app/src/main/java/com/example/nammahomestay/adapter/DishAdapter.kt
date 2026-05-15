package com.example.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.Dish

class DishAdapter(
    private val dishes: List<Dish>,
    private val onEdit: (Dish) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<DishAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivDishImage: ImageView = view.findViewById(R.id.ivDishImage)
        val tvDishName: TextView = view.findViewById(R.id.tvDishName)
        val tvDishDescription: TextView = view.findViewById(R.id.tvDishDescription)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditDish)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteDish)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
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

        holder.btnEdit.setOnClickListener { onEdit(dish) }
        holder.btnDelete.setOnClickListener { onDelete(dish.id) }
    }

    override fun getItemCount() = dishes.size
}
