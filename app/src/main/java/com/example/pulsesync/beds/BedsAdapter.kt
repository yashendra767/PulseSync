package com.example.pulsesync.beds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R

class BedsAdapter(
    private val bedsList: List<BedItem>
) : RecyclerView.Adapter<BedsAdapter.BedsViewHolder>() {

    inner class BedsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bedIdText: TextView = itemView.findViewById(R.id.tvBedId)
        val bedTypeText: TextView = itemView.findViewById(R.id.tvBedType)
        val statusText: TextView = itemView.findViewById(R.id.tvBedStatus)
        val patientNameText: TextView = itemView.findViewById(R.id.tvPatientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BedsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bed_item, parent, false)
        return BedsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BedsViewHolder, position: Int) {
        val bedItem = bedsList[position]
        holder.bedIdText.text = "Bed ID: ${bedItem.bedId}"
        holder.bedTypeText.text = "Type: ${bedItem.type}"
        holder.statusText.text = "Status: ${bedItem.status}"
        holder.patientNameText.text = "Patient: ${bedItem.patientName}"
    }

    override fun getItemCount(): Int = bedsList.size
}
