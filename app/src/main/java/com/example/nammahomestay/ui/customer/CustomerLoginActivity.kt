package com.example.nammahomestay.ui.customer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.google.firebase.auth.FirebaseAuth

class CustomerLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
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
        setContentView(R.layout.activity_customer_login)

        auth = FirebaseAuth.getInstance()

        ivSlideshow = findViewById(R.id.ivSlideshow)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerText = findViewById<TextView>(R.id.registerText)

        loginBtn.setOnClickListener {
            val em = email.text.toString().trim()
            val pw = password.text.toString().trim()

            if (em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false
            loginBtn.text = "Signing in..."

            auth.signInWithEmailAndPassword(em, pw)
                .addOnSuccessListener {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CustomerHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    loginBtn.isEnabled = true
                    loginBtn.text = "Login"
                    Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        registerText.setOnClickListener {
            startActivity(Intent(this, CustomerRegisterActivity::class.java))
        }

        // Start Slideshow
        handler.postDelayed(slideshowRunnable, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(slideshowRunnable)
    }
}
