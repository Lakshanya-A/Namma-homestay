package com.example.nammahomestay.ui.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.BookingAdapter
import com.example.nammahomestay.data.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerBookingsActivity : AppCompatActivity() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var adapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_bookings)

        rvBookings = findViewById(R.id.rvCustomerBookings)
        rvBookings.layoutManager = LinearLayoutManager(this)
        
        // isHost = false means confirm/reject buttons won't show
        adapter = BookingAdapter(bookingList, isHost = false)
        rvBookings.adapter = adapter

        loadMyBookings()
    }

    private fun loadMyBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("bookings")
            .whereEqualTo("customerId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    bookingList.clear()
                    for (doc in snapshot) {
                        val booking = doc.toObject(Booking::class.java)
                        bookingList.add(booking)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
