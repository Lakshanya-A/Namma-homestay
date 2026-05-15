package com.example.nammahomestay.data.remote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.example.nammahomestay.ui.customer.CustomerLoginActivity
import com.example.nammahomestay.ui.host.HostLoginActivity

class RoleSelectionActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_role_selection)

        ivSlideshow = findViewById(R.id.ivSlideshow)
        val btnHost = findViewById<Button>(R.id.btnHost)
        val btnCustomer = findViewById<Button>(R.id.btnGuest)

        btnHost.setOnClickListener {
            startActivity(Intent(this, HostLoginActivity::class.java))
        }

        btnCustomer.setOnClickListener {
            startActivity(Intent(this, CustomerLoginActivity::class.java))
        }

        // Start Slideshow
        handler.postDelayed(slideshowRunnable, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(slideshowRunnable)
    }
}
