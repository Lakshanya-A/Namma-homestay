package com.example.nammahomestay.ui.host

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.google.firebase.firestore.FirebaseFirestore

class EditHomestayActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var homestayId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_homestay)

        homestayId = intent.getStringExtra("HOMESTAY_ID") ?: ""
        
        etName = findViewById(R.id.etEditName)
        etLocation = findViewById(R.id.etEditLocation)
        etPrice = findViewById(R.id.etEditPrice)
        etDescription = findViewById(R.id.etEditDescription)
        btnSave = findViewById(R.id.btnSaveEdit)

        // Pre-fill existing data
        etName.setText(intent.getStringExtra("NAME"))
        etLocation.setText(intent.getStringExtra("LOCATION"))
        etPrice.setText(intent.getStringExtra("PRICE"))
        etDescription.setText(intent.getStringExtra("DESCRIPTION"))

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newLocation = etLocation.text.toString().trim()
            val newPrice = etPrice.text.toString().trim()
            val newDescription = etDescription.text.toString().trim()

            if (newName.isEmpty() || newLocation.isEmpty() || newPrice.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateHomestay(newName, newLocation, newPrice, newDescription)
        }
    }

    private fun updateHomestay(name: String, location: String, price: String, description: String) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "location" to location,
            "price" to price,
            "description" to description
        )

        FirebaseFirestore.getInstance().collection("homestays").document(homestayId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Homestay updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
