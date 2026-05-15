package com.example.nammahomestay.ui.customer

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.nammahomestay.R
import com.example.nammahomestay.adapter.NearbySpotAdapter
import com.example.nammahomestay.adapter.CustomerDishAdapter
import com.example.nammahomestay.adapter.ImageSliderAdapter
import com.example.nammahomestay.data.model.Booking
import com.example.nammahomestay.data.model.Dish
import com.example.nammahomestay.data.model.Homestay
import com.example.nammahomestay.data.model.NearbySpot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomestayDetailActivity : AppCompatActivity() {

    private var homestayLocation: String = ""
    private var homestayName: String = ""
    private var homestayId: String = ""
    private var hostId: String = ""

    private lateinit var rvNearbySpots: RecyclerView
    private lateinit var nearbyAdapter: NearbySpotAdapter
    private val nearbySpotsList = mutableListOf<NearbySpot>()

    private lateinit var rvFoodMenu: RecyclerView
    private lateinit var menuAdapter: CustomerDishAdapter
    private val menuList = mutableListOf<Dish>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homestay_detail)

        homestayId = intent.getStringExtra("ID") ?: ""
        homestayName = intent.getStringExtra("NAME") ?: "Homestay"
        homestayLocation = intent.getStringExtra("LOCATION") ?: "Local Area"
        hostId = intent.getStringExtra("HOST_ID") ?: ""
        val price = intent.getStringExtra("PRICE") ?: "0"
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val imageUrls = intent.getStringArrayListExtra("IMAGE_URLS") ?: arrayListOf<String>()

        val vpImageSlider = findViewById<ViewPager2>(R.id.vpImageSlider)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val btnInquire = findViewById<Button>(R.id.btnInquire)
        val btnBookNow = findViewById<Button>(R.id.btnBookNow)
        
        val tvNearbyTitle = findViewById<TextView>(R.id.tvNearbyTitle)
        rvNearbySpots = findViewById(R.id.rvDetailNearbySpots)
        
        val tvMenuTitle = findViewById<TextView>(R.id.tvMenuTitle)
        rvFoodMenu = findViewById(R.id.rvFoodMenu)

        // Setup Image Slider
        if (imageUrls.isNotEmpty()) {
            val sliderAdapter = ImageSliderAdapter(imageUrls)
            vpImageSlider.adapter = sliderAdapter
        }

        rvNearbySpots.layoutManager = LinearLayoutManager(this)
        nearbyAdapter = NearbySpotAdapter(nearbySpotsList)
        rvNearbySpots.adapter = nearbyAdapter

        rvFoodMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        menuAdapter = CustomerDishAdapter(menuList)
        rvFoodMenu.adapter = menuAdapter

        tvName.text = homestayName
        tvLocation.text = "📍 $homestayLocation"
        tvPrice.text = "₹$price per night"
        tvDescription.text = description

        fetchHomestayDetails(homestayId, tvNearbyTitle)
        fetchFoodMenu(homestayId, tvMenuTitle)

        btnInquire.setOnClickListener {
            val intent = Intent(this, CustomerInquiryActivity::class.java)
            intent.putExtra("HOMESTAY_ID", homestayId)
            intent.putExtra("HOST_ID", hostId)
            startActivity(intent)
        }

        btnBookNow.setOnClickListener { showBookingDialog() }
    }

    private fun fetchHomestayDetails(id: String, titleView: TextView) {
        if (id.isEmpty()) return
        FirebaseFirestore.getInstance().collection("homestays").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val homestay = doc.toObject(Homestay::class.java)
                if (homestay != null && homestay.nearbySpots.isNotEmpty()) {
                    nearbySpotsList.clear()
                    nearbySpotsList.addAll(homestay.nearbySpots)
                    nearbyAdapter.notifyDataSetChanged()
                    titleView.visibility = View.VISIBLE
                    rvNearbySpots.visibility = View.VISIBLE
                }
            }
    }

    private fun fetchFoodMenu(id: String, titleView: TextView) {
        FirebaseFirestore.getInstance().collection("homestays").document(id)
            .collection("menu")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    menuList.clear()
                    for (doc in snapshot) {
                        menuList.add(doc.toObject(Dish::class.java))
                    }
                    menuAdapter.notifyDataSetChanged()
                    titleView.visibility = View.VISIBLE
                    rvFoodMenu.visibility = View.VISIBLE
                }
            }
    }

    private fun showBookingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_book_now, null)
        val etGuestName = dialogView.findViewById<EditText>(R.id.etGuestName)
        val btnCheckIn = dialogView.findViewById<Button>(R.id.btnCheckInDate)
        val btnCheckOut = dialogView.findViewById<Button>(R.id.btnCheckOutDate)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitBooking)
        
        var checkInDate = ""
        var checkOutDate = ""
        val calendar = Calendar.getInstance()

        btnCheckIn.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                checkInDate = "$d/${m+1}/$y"
                btnCheckIn.text = checkInDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnCheckOut.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                checkOutDate = "$d/${m+1}/$y"
                btnCheckOut.text = checkOutDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Book $homestayName")
            .create()

        btnSubmit.setOnClickListener {
            val guestName = etGuestName.text.toString().trim()
            if (guestName.isEmpty() || checkInDate.isEmpty() || checkOutDate.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(
                id = UUID.randomUUID().toString(),
                homestayId = homestayId,
                homestayName = homestayName,
                customerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                customerName = guestName,
                hostId = hostId,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                status = "PENDING"
            )

            FirebaseFirestore.getInstance().collection("bookings").document(booking.id)
                .set(booking)
                .addOnSuccessListener {
                    Toast.makeText(this, "Booking request sent!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
        }
        dialog.show()
    }
}
