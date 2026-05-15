package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.ImageAdapter
import com.example.nammahomestay.adapter.NearbySpotAdapter
import com.example.nammahomestay.ai.agents.TourismAgent
import com.example.nammahomestay.data.model.Homestay
import com.example.nammahomestay.data.model.NearbySpot
import com.example.nammahomestay.data.repository.HomestayRepository
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class AddHomestayActivity : AppCompatActivity() {

    private lateinit var rvImages: RecyclerView
    private lateinit var tvImageCount: TextView
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter

    private lateinit var llAIOptions: LinearLayout
    private lateinit var cardOption1: MaterialCardView
    private lateinit var cardOption2: MaterialCardView
    private lateinit var cardOption3: MaterialCardView
    private lateinit var tvOption1: TextView
    private lateinit var tvOption2: TextView
    private lateinit var tvOption3: TextView
    private lateinit var etDescription: EditText
    private lateinit var pbAiLoading: ProgressBar

    private lateinit var rvNearbySpots: RecyclerView
    private lateinit var nearbySpotAdapter: NearbySpotAdapter
    private val generatedNearbySpots = mutableListOf<NearbySpot>()
    private lateinit var tvNearbySpotsTitle: TextView

    private var option1Content: String = ""
    private var option2Content: String = ""
    private var option3Content: String = ""

    private val PICK_IMAGES_CODE = 1001
    private lateinit var openRouterService: OpenRouterService
    private lateinit var tourismAgent: TourismAgent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_homestay)

        val etName = findViewById<EditText>(R.id.etName)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        etDescription = findViewById(R.id.etDescription)
        pbAiLoading = findViewById(R.id.pbAiLoading)

        val btnUpload = findViewById<Button>(R.id.btnUploadImage)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnSpotsToVisit = findViewById<Button>(R.id.btnSpotsToVisit)
        val btnAIDescription = findViewById<Button>(R.id.btnAIDescription)
        
        rvImages = findViewById(R.id.rvImages)
        tvImageCount = findViewById(R.id.tvImageCount)

        llAIOptions = findViewById(R.id.llAIOptions)
        cardOption1 = findViewById(R.id.cardOption1)
        cardOption2 = findViewById(R.id.cardOption2)
        cardOption3 = findViewById(R.id.cardOption3)
        tvOption1 = findViewById(R.id.tvOption1)
        tvOption2 = findViewById(R.id.tvOption2)
        tvOption3 = findViewById(R.id.tvOption3)

        rvNearbySpots = findViewById(R.id.rvNearbySpots)
        tvNearbySpotsTitle = findViewById(R.id.tvNearbySpotsTitle)
        nearbySpotAdapter = NearbySpotAdapter(generatedNearbySpots)
        rvNearbySpots.layoutManager = LinearLayoutManager(this)
        rvNearbySpots.adapter = nearbySpotAdapter

        imageAdapter = ImageAdapter(selectedImages)
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter

        openRouterService = OpenRouterService(this)
        tourismAgent = TourismAgent(this)

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_CODE)
        }

        btnSpotsToVisit.setOnClickListener {
            val location = etLocation.text.toString().trim()
            if (location.isEmpty()) {
                Toast.makeText(this, "Enter location first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSpotsToVisit.text = "Searching spots..."
            btnSpotsToVisit.isEnabled = false
            pbAiLoading.visibility = View.VISIBLE
            
            tourismAgent.autoGenerateNearbySpots(location) { spots ->
                runOnUiThread {
                    btnSpotsToVisit.text = "✨ Suggest Places to Visit (AI)"
                    btnSpotsToVisit.isEnabled = true
                    pbAiLoading.visibility = View.GONE
                    if (spots.isNotEmpty()) {
                        generatedNearbySpots.clear()
                        generatedNearbySpots.addAll(spots.take(3))
                        tvNearbySpotsTitle.visibility = View.VISIBLE
                        rvNearbySpots.visibility = View.VISIBLE
                        nearbySpotAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "AI is busy. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnAIDescription.setOnClickListener {
            val name = etName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val price = etPrice.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Fill name, location and price first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAIDescription.text = "Writing description..."
            btnAIDescription.isEnabled = false
            pbAiLoading.visibility = View.VISIBLE

            val prompt = """
                Write 3 attractive homestay descriptions for: $name in $location at ₹$price.
                Format:
                Option 1: [content]
                ###
                Option 2: [content]
                ###
                Option 3: [content]
            """.trimIndent()

            openRouterService.generateDescription(prompt) { result ->
                runOnUiThread {
                    btnAIDescription.text = "✨ Write Description (AI)"
                    btnAIDescription.isEnabled = true
                    pbAiLoading.visibility = View.GONE
                    if (result.contains("Error") || result.contains("failed")) {
                         Toast.makeText(this, "AI service temporarily busy.", Toast.LENGTH_SHORT).show()
                    } else {
                        parseAIOptions(result)
                    }
                }
            }
        }

        cardOption1.setOnClickListener { selectOption(1) }
        cardOption2.setOnClickListener { selectOption(2) }
        cardOption3.setOnClickListener { selectOption(3) }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val hostId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (name.isEmpty() || location.isEmpty() || price.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImages.isEmpty()) {
                Toast.makeText(this, "Please add at least one photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Publishing..."

            HomestayRepository.uploadImages(selectedImages, { imageUrls ->
                val homestay = Homestay(
                    hostId = hostId,
                    name = name,
                    location = location,
                    price = price,
                    description = description,
                    imageUrls = imageUrls,
                    nearbySpots = generatedNearbySpots
                )

                HomestayRepository.addHomestay(homestay) { success ->
                    if (success) {
                        Toast.makeText(this, "Published Successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        btnSave.isEnabled = true
                        btnSave.text = "Save & Publish"
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
                }
            }, { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun parseAIOptions(result: String) {
        val options = result.split("###")
        if (options.size >= 3) {
            option1Content = options[0].replace("Option 1:", "").trim()
            option2Content = options[1].replace("Option 2:", "").trim()
            option3Content = options[2].replace("Option 3:", "").trim()

            tvOption1.text = option1Content
            tvOption2.text = option2Content
            tvOption3.text = option3Content
            llAIOptions.visibility = View.VISIBLE
        } else {
            etDescription.setText(result)
            llAIOptions.visibility = View.GONE
        }
    }

    private fun selectOption(index: Int) {
        val selectedText = when(index) {
            1 -> option1Content
            2 -> option2Content
            else -> option3Content
        }
        etDescription.setText(selectedText)
        
        cardOption1.setStrokeWidth(if (index == 1) 6 else 1)
        cardOption1.setStrokeColor(if (index == 1) Color.GREEN else Color.parseColor("#7E57C2"))
        
        cardOption2.setStrokeWidth(if (index == 2) 6 else 1)
        cardOption2.setStrokeColor(if (index == 2) Color.GREEN else Color.parseColor("#7E57C2"))
        
        cardOption3.setStrokeWidth(if (index == 3) 6 else 1)
        cardOption3.setStrokeColor(if (index == 3) Color.GREEN else Color.parseColor("#7E57C2"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_CODE && resultCode == Activity.RESULT_OK) {
            if (data?.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    selectedImages.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                selectedImages.add(data.data!!)
            }
            imageAdapter.notifyDataSetChanged()
            tvImageCount.text = "${selectedImages.size} images selected"
        }
    }
}
