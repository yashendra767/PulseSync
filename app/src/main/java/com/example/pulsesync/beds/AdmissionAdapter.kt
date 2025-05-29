package com.example.pulsesync.beds

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R


class AdmissionAdapter(
    private val admissionsList: List<AdmissionItem>, private val onDeleteClick: (AdmissionItem) -> Unit
) : RecyclerView.Adapter<AdmissionAdapter.AdmissionsViewHolder>() {

    inner class AdmissionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientNameText: TextView = itemView.findViewById(R.id.tvPatientName)
        val bedIdText: TextView = itemView.findViewById(R.id.tvBedId)
        val dateText: TextView = itemView.findViewById(R.id.tvAdmissionDate)
        val statusText: TextView = itemView.findViewById(R.id.tvAdmissionStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdmissionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admission_item, parent, false)
        return AdmissionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdmissionsViewHolder, position: Int) {
        val admissionItem = admissionsList[position]
        holder.patientNameText.text = "Patient: ${admissionItem.patientName}"
        holder.bedIdText.text = "Bed ID: ${admissionItem.bedId}"
        holder.dateText.text = "Date: ${admissionItem.date}"
        holder.statusText.text = "Status: ${admissionItem.status}"

        holder.itemView.findViewById<ImageView>(R.id.deleteAdmission).setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirm Discharge")
                .setMessage("Are you sure you want to discharge this patient?")
                .setPositiveButton("Discharge") { _, _ ->
                    onDeleteClick(admissionItem)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int = admissionsList.size
}
