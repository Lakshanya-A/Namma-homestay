package com.example.nammahomestay.ui.host

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.DishAdapter
import com.example.nammahomestay.data.model.Dish
import com.google.firebase.firestore.FirebaseFirestore

class EditMenuActivity : AppCompatActivity() {

    private lateinit var rvDishes: RecyclerView
    private lateinit var adapter: DishAdapter
    private val dishList = mutableListOf<Dish>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var homestayId: String
    private lateinit var homestayName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_menu)

        homestayId = intent.getStringExtra("HOMESTAY_ID") ?: ""
        homestayName = intent.getStringExtra("HOMESTAY_NAME") ?: "Homestay"

        if (homestayId.isEmpty()) {
            Toast.makeText(this, "Error: Homestay not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.tvMenuHeader).text = "🍲 $homestayName Menu"

        rvDishes = findViewById(R.id.rvDishes)
        rvDishes.layoutManager = LinearLayoutManager(this)

        adapter = DishAdapter(dishList, 
            onEdit = { dish ->
                val intent = Intent(this, AddDishActivity::class.java).apply {
                    putExtra("HOMESTAY_ID", homestayId)
                    putExtra("DISH_ID", dish.id)
                    putExtra("DISH_NAME", dish.dishName)
                    putExtra("DISH_DESC", dish.description)
                    putExtra("DISH_IMAGE", dish.imageUrl)
                    putExtra("IS_EDIT", true)
                }
                startActivity(intent)
            },
            onDelete = { dishId ->
                showDeleteConfirmation(dishId)
            }
        )
        rvDishes.adapter = adapter

        findViewById<Button>(R.id.btnAddNewDish).setOnClickListener {
            val intent = Intent(this, AddDishActivity::class.java).apply {
                putExtra("HOMESTAY_ID", homestayId)
                putExtra("HOMESTAY_NAME", homestayName)
                putExtra("IS_EDIT", false)
            }
            startActivity(intent)
        }

        loadMenu()
    }

    override fun onResume() {
        super.onResume()
        loadMenu()
    }

    private fun loadMenu() {
        db.collection("homestays").document(homestayId).collection("menu")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    dishList.clear()
                    for (doc in snapshot) {
                        val dish = doc.toObject(Dish::class.java).copy(id = doc.id)
                        dishList.add(dish)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun showDeleteConfirmation(dishId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Dish")
            .setMessage("Are you sure you want to remove this dish from the menu?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("homestays").document(homestayId)
                    .collection("menu").document(dishId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Dish removed", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
