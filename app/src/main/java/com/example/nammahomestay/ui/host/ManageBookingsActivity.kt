package com.example.nammahomestay.ui.host

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.BookingAdapter
import com.example.nammahomestay.data.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageBookingsActivity : AppCompatActivity() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var adapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bookings)

        rvBookings = findViewById(R.id.rvManageBookings)
        rvBookings.layoutManager = LinearLayoutManager(this)

        adapter = BookingAdapter(bookingList, isHost = true,
            onConfirm = { booking -> updateBookingStatus(booking, "CONFIRMED") },
            onReject = { booking -> updateBookingStatus(booking, "REJECTED") }
        )
        rvBookings.adapter = adapter

        loadBookings()
    }

    private fun loadBookings() {
        val hostId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("bookings")
            .whereEqualTo("hostId", hostId)
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

    private fun updateBookingStatus(booking: Booking, status: String) {
        db.collection("bookings").document(booking.id)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update booking", Toast.LENGTH_SHORT).show()
            }
    }
}
