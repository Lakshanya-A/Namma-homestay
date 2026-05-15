package com.example.nammahomestay.ui.customer

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.ChatMessage
import com.example.nammahomestay.data.model.Inquiry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerInquiryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_inquiry)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        val homestayId = intent.getStringExtra("HOMESTAY_ID") ?: ""
        val hostId = intent.getStringExtra("HOST_ID") ?: ""
        val customerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        btnSend.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val messageText = etMessage.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || messageText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (hostId.isEmpty()) {
                Toast.makeText(this, "Error: Host information missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            btnSend.text = "Sending..."

            val inquiryId = db.collection("inquiries").document().id
            val inquiry = Inquiry(
                id = inquiryId,
                customerId = customerId,
                name = name,
                phone = phone,
                message = messageText,
                homestayId = homestayId,
                hostId = hostId,
                lastMessage = messageText,
                timestamp = System.currentTimeMillis()
            )

            // Save inquiry document
            db.collection("inquiries").document(inquiryId)
                .set(inquiry)
                .addOnSuccessListener {
                    // Add first message to the messages sub-collection
                    val firstMsg = ChatMessage(
                        senderId = customerId,
                        message = messageText,
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("inquiries").document(inquiryId).collection("messages").add(firstMsg)

                    Toast.makeText(this, "Inquiry Sent Successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    btnSend.isEnabled = true
                    btnSend.text = "Send Inquiry"
                    Toast.makeText(this, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
