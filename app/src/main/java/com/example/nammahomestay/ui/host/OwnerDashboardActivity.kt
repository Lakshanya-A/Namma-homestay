package com.example.nammahomestay.ui.host

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.example.nammahomestay.data.remote.RoleSelectionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var cvNotificationBox: View
    private lateinit var tvBookingNotification: TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, HostLoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_owner_dashboard)

        ivSlideshow = findViewById(R.id.ivSlideshow)

        cvNotificationBox = findViewById(R.id.cvNotificationBox)
        tvBookingNotification = findViewById(R.id.tvBookingNotification)

        findViewById<Button>(R.id.btnConfirmBookings).setOnClickListener {
            startActivity(Intent(this, ManageBookingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddHomestay).setOnClickListener {
            startActivity(Intent(this, AddHomestayActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewMyHomestays).setOnClickListener {
            startActivity(Intent(this, MyHomestaysActivity::class.java))
        }

        findViewById<Button>(R.id.btnInquiry).setOnClickListener {
            startActivity(Intent(this, EnquiryActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        listenForNewBookings()
        
        // Start Slideshow
        handler.postDelayed(slideshowRunnable, 5000)
    }

    private fun listenForNewBookings() {
        val hostId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        FirebaseFirestore.getInstance().collection("bookings")
            .whereEqualTo("hostId", hostId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val count = snapshot.size()
                    tvBookingNotification.text = "You have $count new booking request(s)!"
                    cvNotificationBox.visibility = View.VISIBLE
                } else {
                    cvNotificationBox.visibility = View.GONE
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(slideshowRunnable)
    }
}
