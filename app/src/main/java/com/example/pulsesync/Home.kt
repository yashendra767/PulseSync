package com.example.pulsesync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pulsesync.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val adminTextView = binding.tvAdminName

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                doc->
                val name = doc.getString("name") ?: "Admin"
                adminTextView.text = "Hello $name"
            }
        fetchStats()
        loadVitalsChart()
        loadAdmissionsChart()
        return binding.root
    }

    private fun fetchStats() {
        db.collection("users").get()
            .addOnSuccessListener {
                binding.tvTotalUsers.text = it.size().toString()
            }.addOnFailureListener {
                binding.tvTotalUsers.text = "Failed"
                Log.e("Home","Failed to load users",it)
                showError("Failed to load users.")
            }

        db.collection("beds")
            .get()
            .addOnSuccessListener {
                binding.tvBedsAvailable.text = it.size().toString()
            }.addOnFailureListener {
                binding.tvBedsAvailable.text = "-"
                showError("Failed to load beds.")
            }

        db.collection("admissions")
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                binding.tvTotalAdmissions.text = "$count"
            }
            .addOnFailureListener {
                binding.tvTotalAdmissions.text = "Error"
                showError("Failed to load admissions.")
            }

        db.collection("inventory")
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                binding.tvTotalInventory.text = "$count"
            }
            .addOnFailureListener {
                binding.tvTotalInventory.text = "Error"
                showError("Failed to load inventory.")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun loadAdmissionsChart() {
        val chart = binding.barChartAdmissions
        FirebaseFirestore.getInstance().collection("admissions")
            .get()
            .addOnSuccessListener { snapshot ->
                val dateFormatIn = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val dateFormatOut = SimpleDateFormat("dd MMM", Locale.getDefault())

                val dataMap = mutableMapOf<String, Int>()

                snapshot.forEach { doc ->
                    val dateStr = doc.getString("date")?.trim() ?: return@forEach
                    try {
                        val parsedDate = dateFormatIn.parse(dateStr)
                        Log.d("AdmissionsChart", "Parsed date: $dateStr -> $parsedDate")
                        val formattedDate = dateFormatOut.format(parsedDate!!)
                        dataMap[formattedDate] = dataMap.getOrDefault(formattedDate, 0) + 1
                    } catch (e: Exception) {
                        Log.w("AdmissionsChart", "Invalid date format: $dateStr")
                    }
                }

                if (dataMap.isEmpty()) {
                    chart.clear()
                    chart.setNoDataText("No admissions data available")
                    return@addOnSuccessListener
                }

                val labels = dataMap.keys.sorted()
                val entries = labels.mapIndexed { index, label ->
                    BarEntry(index.toFloat(), dataMap[label]?.toFloat() ?: 0f)
                }

                val dataSet = BarDataSet(entries, "Admissions")
                dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary_yellow)

                chart.apply {
                    data = BarData(dataSet)
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    animateY(1000)
                    invalidate()
                }
            }
            .addOnFailureListener {
                chart.setNoDataText("Error loading admissions")
            }
    }


    private fun loadVitalsChart() {
        val chart = binding.lineChartVitals
        FirebaseFirestore.getInstance().collection("admissions")
            .orderBy("lastUpdated", Query.Direction.ASCENDING)
            .limit(15)
            .get()
            .addOnSuccessListener { snapshot ->
                val tempEntries = mutableListOf<Entry>()
                val oxygenEntries = mutableListOf<Entry>()
                val bpEntries = mutableListOf<Entry>()
                val labels = mutableListOf<String>()

                snapshot.forEachIndexed { index, doc ->
                    val temp = doc.getDouble("temperature")
                    val oxygen = doc.getDouble("oxygen")
                    val bp = doc.getString("bloodPressure")
                    val lastUpdated = doc.getLong("lastUpdated")
                    if (temp == null || oxygen == null || bp == null || lastUpdated == null) {
                        Log.w("VitalsChart", "Skipping due to missing fields in doc: ${doc.id}")
                        return@forEachIndexed
                    }
                    val systolic = bp.split("/").getOrNull(0)?.toFloatOrNull()
                    if (systolic == null) {
                        Log.w("VitalsChart", "Skipping invalid BP: $bp in doc: ${doc.id}")
                        return@forEachIndexed
                    }
                    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault())
                        .format(Date(lastUpdated))

                    labels.add(dateStr)
                    tempEntries.add(Entry(index.toFloat(), temp.toFloat()))
                    oxygenEntries.add(Entry(index.toFloat(), oxygen.toFloat()))
                    bpEntries.add(Entry(index.toFloat(), systolic))
                }

                val tempDataSet = LineDataSet(tempEntries, "Temperature (°C)").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.primary_red)
                    setCircleColor(color)
                    lineWidth = 2f
                    circleRadius = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }

                val oxygenDataSet = LineDataSet(oxygenEntries, "Oxygen Level (%)").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.primary_blue)
                    setCircleColor(color)
                    lineWidth = 2f
                    circleRadius = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }

                val bpDataSet = LineDataSet(bpEntries, "Systolic BP (mmHg)").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.primary_green)
                    setCircleColor(color)
                    lineWidth = 2f
                    circleRadius = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }

                val lineData = LineData(tempDataSet, oxygenDataSet, bpDataSet)

                chart.apply {
                    data = lineData
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = true
                    animateX(1000)
                    invalidate()
                }
            }
            .addOnFailureListener {
                Log.e("VitalsChart", "Error loading vitals chart", it)
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
