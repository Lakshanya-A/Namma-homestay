package com.example.nammahomestay.ui.customer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.HomestayAdapter
import com.example.nammahomestay.data.model.Homestay
import com.example.nammahomestay.data.model.Inquiry
import com.example.nammahomestay.data.remote.RoleSelectionActivity
import com.example.nammahomestay.data.repository.HomestayRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList

class CustomerHomeActivity : AppCompatActivity() {

    private lateinit var rvHomestays: RecyclerView
    private lateinit var adapter: HomestayAdapter
    private val allHomestays = mutableListOf<Homestay>()
    private val filteredHomestays = mutableListOf<Homestay>()
    private lateinit var pbLoading: ProgressBar

    private lateinit var ivSlideshow: ImageView
    private val images = listOf(
        R.drawable.place2,
        R.drawable.place1,
        R.drawable.place3,
        R.drawable.place4,
        R.drawable.place5
    )
    private var currentImageIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val slideshowRunnable = object : Runnable {
        override fun run() {
            currentImageIndex = (currentImageIndex + 1) % images.size
            ivSlideshow.setImageResource(images[currentImageIndex])
            handler.postDelayed(this, 5000)
        }
    }

    private lateinit var cvChatNotification: View
    private lateinit var tvChatLastMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_home)

        // Auth Check
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, CustomerLoginActivity::class.java))
            finish()
            return
        }

        ivSlideshow = findViewById(R.id.ivSlideshow)
        rvHomestays = findViewById(R.id.rvCustomerHomestays)
        pbLoading = findViewById(R.id.pbHomeLoading)
        val etSearch = findViewById<EditText>(R.id.etSearchLocation)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val btnMyBookings = findViewById<Button>(R.id.btnMyBookings)
        
        cvChatNotification = findViewById(R.id.cvChatNotification)
        tvChatLastMsg = findViewById(R.id.tvChatLastMsg)

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnMyBookings.setOnClickListener {
            startActivity(Intent(this, CustomerBookingsActivity::class.java))
        }

        rvHomestays.layoutManager = LinearLayoutManager(this)
        adapter = HomestayAdapter(filteredHomestays) { homestay ->
            val intent = Intent(this, HomestayDetailActivity::class.java).apply {
                putExtra("ID", homestay.id)
                putExtra("NAME", homestay.name)
                putExtra("LOCATION", homestay.location)
                putExtra("PRICE", homestay.price)
                putExtra("DESCRIPTION", homestay.description)
                putExtra("HOST_ID", homestay.hostId)
                putStringArrayListExtra("IMAGE_URLS", ArrayList(homestay.imageUrls))
            }
            startActivity(intent)
        }
        rvHomestays.adapter = adapter

        loadHomestays()
        listenForChatReplies(currentUser.uid)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHomestays(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Start Slideshow
        handler.postDelayed(slideshowRunnable, 5000)
    }

    private fun listenForChatReplies(userId: String) {
        FirebaseFirestore.getInstance().collection("inquiries")
            .whereEqualTo("customerId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val inquiry = snapshot.documents[0].toObject(Inquiry::class.java)?.copy(id = snapshot.documents[0].id)
                    if (inquiry != null && inquiry.lastMessage.isNotEmpty()) {
                        cvChatNotification.visibility = View.VISIBLE
                        tvChatLastMsg.text = inquiry.lastMessage
                        
                        cvChatNotification.setOnClickListener {
                            val intent = Intent(this, ChatActivity::class.java).apply {
                                putExtra("INQUIRY_ID", inquiry.id)
                                putExtra("HOST_NAME", "Host") // Could fetch actual name if stored
                            }
                            startActivity(intent)
                        }
                    } else {
                        cvChatNotification.visibility = View.GONE
                    }
                } else {
                    cvChatNotification.visibility = View.GONE
                }
            }
    }

    private fun loadHomestays() {
        pbLoading.visibility = View.VISIBLE
        HomestayRepository.getHomestays { list ->
            runOnUiThread {
                pbLoading.visibility = View.GONE
                allHomestays.clear()
                allHomestays.addAll(list)
                filterHomestays("") // Initial show all
            }
        }
    }

    private fun filterHomestays(query: String) {
        filteredHomestays.clear()
        if (query.isEmpty()) {
            filteredHomestays.addAll(allHomestays)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (homestay in allHomestays) {
                if (homestay.location.lowercase().contains(lowerCaseQuery) || 
                    homestay.name.lowercase().contains(lowerCaseQuery)) {
                    filteredHomestays.add(homestay)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(slideshowRunnable)
    }
}
