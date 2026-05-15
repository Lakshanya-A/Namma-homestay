package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.ChatAdapter
import com.example.nammahomestay.data.model.ChatMessage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ReplyActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etReply: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnMic: FloatingActionButton
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    private var inquiryId: String = ""
    private var guestName: String = ""
    private val SPEECH_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply)

        inquiryId = intent.getStringExtra("INQUIRY_ID") ?: ""
        guestName = intent.getStringExtra("GUEST_NAME") ?: "Guest"

        val toolbar = findViewById<Toolbar>(R.id.toolbarReply)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chat with $guestName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvMessages = findViewById(R.id.rvChatMessagesHost)
        etReply = findViewById(R.id.etReplyMessage)
        btnMic = findViewById(R.id.btnMic)
        btnSend = findViewById(R.id.btnSendReply)

        adapter = ChatAdapter(messageList, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        if (inquiryId.isNotEmpty()) {
            listenForMessages()
        }

        btnMic.setOnClickListener { startVoiceToText() }
        btnSend.setOnClickListener { sendMessage() }
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
        val text = etReply.text.toString().trim()
        if (text.isEmpty()) return

        val message = ChatMessage(
            senderId = currentUserId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        etReply.setText("")

        db.collection("inquiries").document(inquiryId).collection("messages")
            .add(message)
            .addOnSuccessListener {
                db.collection("inquiries").document(inquiryId)
                    .update("lastMessage", text, "timestamp", System.currentTimeMillis())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startVoiceToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your reply...")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            val currentText = etReply.text.toString()
            etReply.setText(if (currentText.isNotEmpty()) "$currentText $spokenText" else spokenText)
            etReply.setSelection(etReply.text.length)
        }
    }
}
