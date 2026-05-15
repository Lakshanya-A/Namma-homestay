package com.example.nammahomestay.ui.host

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.HomestayAdapter
import com.example.nammahomestay.data.model.Homestay
import com.example.nammahomestay.data.repository.HomestayRepository
import com.google.firebase.auth.FirebaseAuth

class MyHomestaysActivity : AppCompatActivity() {

    private lateinit var rvMyHomestays: RecyclerView
    private lateinit var adapter: HomestayAdapter
    private val myHomestays = mutableListOf<Homestay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_homestays)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        rvMyHomestays = findViewById(R.id.rvMyHomestaysList)
        rvMyHomestays.layoutManager = LinearLayoutManager(this)

        adapter = HomestayAdapter(myHomestays) { homestay ->
            // Handle Edit Homestay click
            val intent = Intent(this, EditHomestayActivity::class.java).apply {
                putExtra("HOMESTAY_ID", homestay.id)
                putExtra("NAME", homestay.name)
                putExtra("LOCATION", homestay.location)
                putExtra("PRICE", homestay.price)
            }
            startActivity(intent)
        }
        rvMyHomestays.adapter = adapter

        loadMyHomestays(user.uid)
    }

    private fun loadMyHomestays(hostId: String) {
        HomestayRepository.getHomestaysByHost(hostId) { list ->
            runOnUiThread {
                myHomestays.clear()
                myHomestays.addAll(list)
                adapter.notifyDataSetChanged()
                if (list.isEmpty()) {
                    Toast.makeText(this, "No homestays found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            loadMyHomestays(user.uid)
        }
    }
}
