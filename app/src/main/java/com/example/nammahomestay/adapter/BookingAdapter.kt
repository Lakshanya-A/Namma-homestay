package com.example.nammahomestay.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.Booking
import com.example.nammahomestay.ui.customer.ChatActivity

class BookingAdapter(
    private val bookings: List<Booking>,
    private val isHost: Boolean,
    private val onConfirm: ((Booking) -> Unit)? = null,
    private val onReject: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHomestayName: TextView = view.findViewById(R.id.tvBookingHomestayName)
        val tvGuestName: TextView = view.findViewById(R.id.tvBookingGuestName)
        val tvDates: TextView = view.findViewById(R.id.tvBookingDates)
        val tvStatus: TextView = view.findViewById(R.id.tvBookingStatus)
        val btnConfirm: ImageButton = view.findViewById(R.id.btnConfirmBooking)
        val btnReject: ImageButton = view.findViewById(R.id.btnRejectBooking)
        val llActions: View = view.findViewById(R.id.llBookingActions)
        val llChatSection: View = view.findViewById(R.id.llChatSection)
        val divider: View = view.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        holder.tvHomestayName.text = booking.homestayName
        holder.tvGuestName.text = if (isHost) "Guest: ${booking.customerName}" else "Booking Request"
        holder.tvDates.text = "${booking.checkInDate} - ${booking.checkOutDate}"
        holder.tvStatus.text = "Status: ${booking.status}"

        if (isHost && booking.status == "PENDING") {
            holder.llActions.visibility = View.VISIBLE
            holder.btnConfirm.setOnClickListener { onConfirm?.invoke(booking) }
            holder.btnReject.setOnClickListener { onReject?.invoke(booking) }
        } else {
            holder.llActions.visibility = View.GONE
        }
        
        // Show chat section for both host and guest if booking exists
        // User specifically asked for guest dashboard below booking status
        if (!isHost) {
            holder.divider.visibility = View.VISIBLE
            holder.llChatSection.visibility = View.VISIBLE
            holder.llChatSection.setOnClickListener {
                val intent = Intent(holder.itemView.context, ChatActivity::class.java).apply {
                    putExtra("bookingId", booking.id)
                    putExtra("hostId", booking.hostId)
                    putExtra("homestayName", booking.homestayName)
                }
                holder.itemView.context.startActivity(intent)
            }
        } else {
            // Optional: Also allow host to chat from their dashboard
            holder.divider.visibility = View.VISIBLE
            holder.llChatSection.visibility = View.VISIBLE
            holder.llChatSection.setOnClickListener {
                val intent = Intent(holder.itemView.context, ChatActivity::class.java).apply {
                    putExtra("bookingId", booking.id)
                    putExtra("hostId", booking.hostId)
                    putExtra("homestayName", "Chat with ${booking.customerName}")
                }
                holder.itemView.context.startActivity(intent)
            }
        }

        // Color status
        when(booking.status) {
            "CONFIRMED" -> holder.tvStatus.setTextColor(0xFF43A047.toInt())
            "REJECTED" -> holder.tvStatus.setTextColor(0xFFD32F2F.toInt())
            else -> holder.tvStatus.setTextColor(0xFF7E57C2.toInt())
        }
    }

    override fun getItemCount() = bookings.size
}
