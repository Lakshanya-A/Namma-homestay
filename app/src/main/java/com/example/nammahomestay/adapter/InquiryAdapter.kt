package com.example.nammahomestay.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammahomestay.R
import com.example.nammahomestay.data.model.Inquiry
import com.example.nammahomestay.ui.host.ReplyActivity

class InquiryAdapter(private val list: List<Inquiry>) :
    RecyclerView.Adapter<InquiryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val message: TextView = view.findViewById(R.id.tvMessage)
        val phone: TextView = view.findViewById(R.id.tvPhone)
        val callBtn: Button = view.findViewById(R.id.btnCall)
        val replyBtn: Button = view.findViewById(R.id.btnReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inquiry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.name
        holder.message.text = item.message
        holder.phone.text = "Phone: ${item.phone}"

        holder.callBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${item.phone}")
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.replyBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ReplyActivity::class.java).apply {
                putExtra("INQUIRY_ID", item.id)
                putExtra("GUEST_NAME", item.name)
                putExtra("GUEST_MESSAGE", item.message)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}
