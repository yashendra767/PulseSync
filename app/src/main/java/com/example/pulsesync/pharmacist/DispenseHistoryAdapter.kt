package com.example.pulsesync.pharmacist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import java.text.SimpleDateFormat
import java.util.*

class DispenseHistoryAdapter(private val items: MutableList<DispenseHistoryItem>) :
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
        val item = items[position]
        holder.itemName.text = item.itemName
        holder.quantity.text = "Dispensed: ${item.quantityDispensed}"
        holder.note.text = if (item.note.isNotBlank()) "Note: ${item.note}" else "Note: –"
        holder.timestamp.text = formatTimestamp(item.timestamp)
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<DispenseHistoryItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatTimestamp(time: Long): String {
        return try {
            if (time <= 0L) "Invalid Time"
            else SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(time))
        } catch (e: Exception) {
            "Invalid Time"
        }
    }
}
