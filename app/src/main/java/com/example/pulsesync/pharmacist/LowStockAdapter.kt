package com.example.pulsesync.pharmacist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.inventory.InventoryItem

class LowStockAdapter(private val items: List<InventoryItem>) : RecyclerView.Adapter<LowStockAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemNameLow)
        val tvQuantity: TextView = view.findViewById(R.id.tvItemQuantityLow)
        val tvCategory: TextView = view.findViewById(R.id.tvItemCategoryLow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_low_stock, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.itemName
        holder.tvQuantity.text = "Qty: ${item.quantity}"
        holder.tvCategory.text = item.category
    }
}
