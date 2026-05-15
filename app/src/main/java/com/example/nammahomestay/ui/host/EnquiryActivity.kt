package com.example.nammahomestay.ui.host

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.InquiryAdapter
import com.example.nammahomestay.data.model.Inquiry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EnquiryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InquiryAdapter
    private val db = FirebaseFirestore.getInstance()
    private val inquiryList = mutableListOf<Inquiry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enquiry)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = InquiryAdapter(inquiryList)
        recyclerView.adapter = adapter

        loadInquiries(user.uid)
    }

    private fun loadInquiries(hostId: String) {
        // Fetch inquiries specifically for this host
        db.collection("inquiries")
            .whereEqualTo("hostId", hostId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // If index is missing, it might fail initially. 
                    // Fallback to simple query if orderBy fails
                    loadInquiriesSimple(hostId)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    inquiryList.clear()
                    for (doc in snapshot) {
                        val inquiry = doc.toObject(Inquiry::class.java).copy(id = doc.id)
                        inquiryList.add(inquiry)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun loadInquiriesSimple(hostId: String) {
        db.collection("inquiries")
            .whereEqualTo("hostId", hostId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    inquiryList.clear()
                    for (doc in snapshot) {
                        val inquiry = doc.toObject(Inquiry::class.java).copy(id = doc.id)
                        inquiryList.add(inquiry)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
