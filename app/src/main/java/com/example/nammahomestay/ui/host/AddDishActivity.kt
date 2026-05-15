package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nammahomestay.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddDishActivity : AppCompatActivity() {

    private lateinit var dishImage: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE = 101
    private lateinit var homestayId: String
    private var dishId: String? = null
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dish)

        homestayId = intent.getStringExtra("HOMESTAY_ID") ?: ""
        dishId = intent.getStringExtra("DISH_ID")
        isEdit = intent.getBooleanExtra("IS_EDIT", false)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val etDishName = findViewById<EditText>(R.id.dishName)
        val etDescription = findViewById<EditText>(R.id.descriptionText)
        val btnUpload = findViewById<Button>(R.id.uploadDishBtn)
        val btnGenerate = findViewById<Button>(R.id.generateBtn)
        val btnSave = findViewById<Button>(R.id.addDishBtn)
        dishImage = findViewById(R.id.dishImage)

        if (isEdit) {
            tvTitle.text = "✏️ Edit Dish"
            btnSave.text = "Update Dish"
            etDishName.setText(intent.getStringExtra("DISH_NAME"))
            etDescription.setText(intent.getStringExtra("DISH_DESC"))
            val imageUrl = intent.getStringExtra("DISH_IMAGE")
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this).load(imageUrl).into(dishImage)
            }
        }

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnGenerate.setOnClickListener {
            val dish = etDishName.text.toString().trim()
            if (dish.isEmpty()) {
                Toast.makeText(this, "Enter dish name first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGenerate.text = "Generating..."
            btnGenerate.isEnabled = false

            val prompt = "Write a one-line mouth-watering authentic caption for $dish at a homestay. Keep it under 15 words."
            OpenRouterService(this).generateDescription(prompt) { result ->
                runOnUiThread {
                    btnGenerate.text = "✨ Generate with AI"
                    btnGenerate.isEnabled = true
                    if (result.startsWith("AI Error") || result.startsWith("Error")) {
                        Toast.makeText(this, "AI is busy. Try again.", Toast.LENGTH_SHORT).show()
                    } else {
                        etDescription.setText(result.trim().replace("\"", ""))
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val name = etDishName.text.toString().trim()
            val desc = etDescription.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDish(name, desc)
        }
    }

    private fun saveDish(name: String, desc: String) {
        val currentDishId = dishId ?: UUID.randomUUID().toString()
        
        if (imageUri != null) {
            val ref = FirebaseStorage.getInstance().reference.child("dishes/$currentDishId.jpg")
            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    updateFirestore(currentDishId, name, desc, uri.toString())
                }
            }
        } else if (isEdit) {
            updateFirestore(currentDishId, name, desc, intent.getStringExtra("DISH_IMAGE") ?: "")
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFirestore(id: String, name: String, desc: String, url: String) {
        val data = hashMapOf(
            "dishName" to name,
            "description" to desc,
            "imageUrl" to url,
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("homestays").document(homestayId)
            .collection("menu").document(id).set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Menu Updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            dishImage.setImageURI(imageUri)
        }
    }
}
