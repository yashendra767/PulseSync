package com.example.pulsesync.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.beds.AdmissionItem

class PatientAdapter(private val patients: List<AdmissionItem>) :
    RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPatientName: TextView = itemView.findViewById(R.id.textPatientName)
        val textPatientCondition: TextView = itemView.findViewById(R.id.textPatientCondition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.patient_item, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.textPatientName.text = patient.patientName ?: "No Name"
        holder.textPatientCondition.text = patient.status ?: "No condition info"
    }

    override fun getItemCount(): Int = patients.size
}