package com.example.pulsesync.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.beds.AdmissionItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorHome : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PatientAdapter
    private val patientsList = mutableListOf<AdmissionItem>()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var recyclerViewAlerts: RecyclerView
    private lateinit var alertAdapter: AlertAdapter
    private val alertList = mutableListOf<AlertItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPatients)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PatientAdapter(patientsList)
        recyclerView.adapter = adapter

        recyclerViewAlerts = view.findViewById(R.id.recyclerViewAlerts)
        recyclerViewAlerts.layoutManager = LinearLayoutManager(requireContext())
        alertAdapter = AlertAdapter(alertList)
        recyclerViewAlerts.adapter = alertAdapter

        fetchAssignedPatients()
        return view
    }

    private fun fetchAssignedPatients() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        firestore.collection("doctors").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener

                val assignedBeds = doc.get("assignedBeds") as? List<String>
                if (!assignedBeds.isNullOrEmpty()) {
                    firestore.collection("admissions")
                        .whereIn("bedId", assignedBeds)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!isAdded) return@addOnSuccessListener

                            patientsList.clear()
                            for (document in documents) {
                                val patient = document.toObject(AdmissionItem::class.java)
                                patientsList.add(patient)
                            }
                            adapter.notifyDataSetChanged()
                            fetchAlerts(assignedBeds)
                            generateAlertsFromVitals(patientsList)
                        }
                        .addOnFailureListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Error fetching patients", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "You are not assigned any beds yet.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error fetching doctor data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchAlerts(assignedBeds: List<String>) {
        firestore.collection("alerts")
            .whereIn("bedId", assignedBeds)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded) return@addOnSuccessListener
                alertList.clear()
                for (doc in documents) {
                    val alert = doc.toObject(AlertItem::class.java)
                    alertList.add(alert)
                }
                alertAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch alerts", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun generateAlertsFromVitals(patients: List<AdmissionItem>) {
        if (!isAdded) return

        val generatedAlerts = mutableListOf<AlertItem>()
        val alertsCollection = firestore.collection("alerts")

        for (patient in patients) {
            val alerts = mutableListOf<String>()
            var severityLevel = "Low"

            if (patient.temperature > 101.5) {
                alerts.add("High temperature: ${patient.temperature}°F")
                severityLevel = "Medium"
            }

            if (patient.oxygen < 92) {
                alerts.add("Low oxygen level: ${patient.oxygen}%")
                severityLevel = "High"
            }

            val bpParts = patient.bloodPressure.split("/")
            if (bpParts.size == 2) {
                val sys = bpParts[0].toIntOrNull()
                val dia = bpParts[1].toIntOrNull()
                if (sys != null && dia != null && (sys > 140 || dia > 90)) {
                    alerts.add("High blood pressure: ${patient.bloodPressure}")
                    if (severityLevel != "High") severityLevel = "Medium"
                }
            }

            if (alerts.isNotEmpty()) {
                val newAlert = AlertItem(
                    title = "Alert for ${patient.patientName}",
                    description = alerts.joinToString("\n"),
                    severity = severityLevel
                )

                generatedAlerts.add(newAlert)

                val alertMap = hashMapOf(
                    "title" to newAlert.title,
                    "description" to newAlert.description,
                    "severity" to newAlert.severity,
                    "bedId" to patient.bedId,
                    "timestamp" to System.currentTimeMillis()
                )

                alertsCollection.add(alertMap)
            }
        }
    alertAdapter.notifyDataSetChanged()
    }
}
