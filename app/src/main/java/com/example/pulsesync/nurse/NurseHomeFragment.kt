package com.example.pulsesync.nurse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.doctor.AlertAdapter
import com.example.pulsesync.doctor.AlertItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NurseHomeFragment : Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvActivePatients: TextView
    private lateinit var rvAlerts: RecyclerView
    private lateinit var alertsAdapter: AlertAdapter
    private val alertList = mutableListOf<AlertItem>()
    private lateinit var tvNoAlerts: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nurse_home, container, false)

        tvWelcome = view.findViewById(R.id.tv_nurse_welcome)
        tvActivePatients = view.findViewById(R.id.tv_active_patients)
        rvAlerts = view.findViewById(R.id.rv_alerts)
        tvNoAlerts = view.findViewById(R.id.tv_no_alerts)

        alertsAdapter = AlertAdapter(alertList)
        rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        rvAlerts.adapter = alertsAdapter

        fetchActiveAdmissions()
        fetchVitalsAlerts()

        return view
    }

    private fun fetchActiveAdmissions() {
        FirebaseFirestore.getInstance().collection("admissions")
            .whereEqualTo("status", "Admitted")
            .get()
            .addOnSuccessListener {
                tvActivePatients.text = "Active Patients: ${it.size()}"
            }
    }

    private fun fetchVitalsAlerts() {
        FirebaseFirestore.getInstance().collection("alerts")
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                alertList.clear()
                for (doc in result) {
                    val alert = doc.toObject(AlertItem::class.java)
                    alertList.add(alert)
                }
                alertsAdapter.notifyDataSetChanged()
                if (alertList.isEmpty()) {
                    rvAlerts.visibility = View.GONE
                    tvNoAlerts.visibility = View.VISIBLE
                } else {
                    rvAlerts.visibility = View.VISIBLE
                    tvNoAlerts.visibility = View.GONE
                }
            }
    }
}
