package com.example.nammahomestay.ui.customer

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.ChatAdapter
import com.example.nammahomestay.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    private var inquiryId: String = ""
    private var hostName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        inquiryId = intent.getStringExtra("INQUIRY_ID") ?: ""
        hostName = intent.getStringExtra("HOST_NAME") ?: "Host"

        val toolbar = findViewById<Toolbar>(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chat with $hostName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvMessages = findViewById(R.id.rvChatMessages)
        etMessage = findViewById(R.id.etChatMessage)
        btnSend = findViewById(R.id.btnSendChat)

        adapter = ChatAdapter(messageList, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        if (inquiryId.isNotEmpty()) {
            listenForMessages()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun listenForMessages() {
        db.collection("inquiries").document(inquiryId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messageList.clear()
                    for (doc in snapshot) {
                        messageList.add(doc.toObject(ChatMessage::class.java))
                    }
                    adapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        rvMessages.smoothScrollToPosition(messageList.size - 1)
                    }
                }
            }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val message = ChatMessage(
            senderId = currentUserId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        etMessage.setText("")

        db.collection("inquiries").document(inquiryId).collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Also update last message in inquiry doc
                db.collection("inquiries").document(inquiryId)
                    .update("lastMessage", text, "timestamp", System.currentTimeMillis())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
            }
    }
}
