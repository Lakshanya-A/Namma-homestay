package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.SecretSpot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddSecretSpotActivity : AppCompatActivity() {

    private lateinit var ivSpotPreview: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE = 103
    private lateinit var openRouterService: OpenRouterService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_secret_spot)

        ivSpotPreview = findViewById(R.id.ivSpotPreview)
        val etName = findViewById<EditText>(R.id.etSpotName)
        val etType = findViewById<EditText>(R.id.etSpotType)
        val etDescription = findViewById<EditText>(R.id.etSpotDescription)
        val btnUpload = findViewById<Button>(R.id.btnUploadSpotImage)
        val btnAI = findViewById<Button>(R.id.btnGenerateSpotAI)
        val btnSave = findViewById<Button>(R.id.btnSaveSpot)

        openRouterService = OpenRouterService(this)

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnAI.setOnClickListener {
            val name = etName.text.toString().trim()
            val type = etType.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Enter name and type first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prompt = """
                Write a mysterious, inviting, and short description for a local secret spot.
                Name: $name
                Type: $type
                Style: Enchanting, local-expert feel, professional.
                Keep it under 30 words.
            """.trimIndent()

            Toast.makeText(this, "Generating mysterious description...", Toast.LENGTH_SHORT).show()
            openRouterService.generateDescription(prompt) { result ->
                etDescription.setText(result.trim().replace("\"", ""))
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val type = etType.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val hostId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (name.isEmpty() || type.isEmpty() || description.isEmpty() || hostId.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill all fields and select a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveSecretSpot(name, type, description, hostId)
        }
    }

    private fun saveSecretSpot(name: String, type: String, description: String, hostId: String) {
        val spotId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("secret_spots/$spotId.jpg")
        val db = FirebaseFirestore.getInstance()

        Toast.makeText(this, "Uploading spot details...", Toast.LENGTH_SHORT).show()

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val spot = SecretSpot(
                        id = spotId,
                        hostId = hostId,
                        name = name,
                        type = type,
                        description = description,
                        imageUrl = uri.toString()
                    )

                    db.collection("secret_spots").document(spotId).set(spot)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Secret Spot Shared!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            ivSpotPreview.setImageURI(imageUri)
        }
    }
}
