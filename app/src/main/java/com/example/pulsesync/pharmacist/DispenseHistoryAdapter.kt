package com.example.pulsesync.pharmacist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import java.text.SimpleDateFormat
import java.util.*

class DispenseHistoryAdapter(private val historyList: List<DispenseHistoryItem>) :
    RecyclerView.Adapter<DispenseHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val quantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val note: TextView = itemView.findViewById(R.id.tvNote)
        val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dispense_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.itemName.text = item.itemName
        holder.quantity.text = "Dispensed: ${item.quantityDispensed}"
        holder.note.text = if (item.note.isNotEmpty()) "Note: ${item.note}" else "Note: -"
        holder.timestamp.text = formatTimestamp(item.timestamp)
    }

    override fun getItemCount(): Int = historyList.size

    private fun formatTimestamp(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }
}
