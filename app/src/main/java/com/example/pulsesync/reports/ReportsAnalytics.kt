package com.example.pulsesync.reports

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.ParseException
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog


class ReportsAnalytics : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerReportType: Spinner
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnGenerateReport: Button
    private lateinit var rvReportData: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var fabAddReport: View
    private lateinit var barChart: BarChart


    private lateinit var reportsAdapter: ReportsAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var selectedReportType = ""
    private var selectedStartDate = ""
    private var selectedEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_analytics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }


        toolbar = findViewById(R.id.toolbar)
        spinnerReportType = findViewById(R.id.spinnerReportType)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        rvReportData = findViewById(R.id.rvReportData)
        tvNoData = findViewById(R.id.tvNoData)
        fabAddReport = findViewById(R.id.fabAddReport)
        val analyticsContainer = findViewById<FrameLayout>(R.id.analyticsContainer)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        populateBedAndAdmissionData()

        barChart = BarChart(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setNoDataText("No analytics data available")
            description.isEnabled = false
            setDrawGridBackground(false)
            setFitBars(true)
            animateY(1000)
            axisRight.isEnabled = false
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
        }
        analyticsContainer.addView(barChart)

        reportsAdapter = ReportsAdapter(emptyList())
        rvReportData.layoutManager = LinearLayoutManager(this)
        rvReportData.adapter = reportsAdapter

        spinnerReportType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedReportType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnStartDate.setOnClickListener {
            showDatePicker { dateStr ->
                selectedStartDate = dateStr
                btnStartDate.text = dateStr
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { dateStr ->
                selectedEndDate = dateStr
                btnEndDate.text = dateStr
            }
        }

        btnGenerateReport.setOnClickListener {
            if (validateInputs()) {
                fetchReports(selectedReportType, selectedStartDate, selectedEndDate)
            }
        }

        fabAddReport.setOnClickListener {
            openAddReportBottomSheet()
        }

        val today = sdf.format(Date())
        selectedStartDate = today
        selectedEndDate = today
        btnStartDate.text = today
        btnEndDate.text = today
    }
    private fun populateBedAndAdmissionData() {
        val tvTotalBeds: TextView = findViewById(R.id.tvTotalBeds)
        val tvOccupiedBeds: TextView = findViewById(R.id.tvOccupiedBeds)
        val tvActiveAdmissions: TextView = findViewById(R.id.tvActiveAdmissions)

        firestore.collection("beds")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val totalBeds = querySnapshot.size()
                val occupiedBeds = querySnapshot.documents.count { doc ->
                    val status = doc.getString("status")?.lowercase(Locale.getDefault())
                    status == "occupied"
                }

                tvTotalBeds.text = totalBeds.toString()
                tvOccupiedBeds.text = occupiedBeds.toString()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch bed data: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        firestore.collection("admissions")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val activeAdmissions = querySnapshot.documents.count { doc ->
                    val status = doc.getString("status")?.lowercase(Locale.getDefault())
                    status == "admitted"
                }

                tvActiveAdmissions.text = activeAdmissions.toString()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch admissions data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openAddReportBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_add_report, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val etTitle: EditText = bottomSheetView.findViewById(R.id.etReportTitle)
        val etPeriod: EditText = bottomSheetView.findViewById(R.id.etReportPeriod)
        val etSummary: EditText = bottomSheetView.findViewById(R.id.etReportSummary)
        val spinnerReportType: Spinner = bottomSheetView.findViewById(R.id.spinnerReportType)
        val btnSelectDate: Button = bottomSheetView.findViewById(R.id.btnSelectDate)
        val btnSubmitReport: Button = bottomSheetView.findViewById(R.id.btnSubmitReport)

        var selectedDate: Timestamp? = null

        val reportTypes = resources.getStringArray(R.array.report_types)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReportType.adapter = spinnerAdapter

        btnSelectDate.setOnClickListener {
            showDatePicker { dateStr ->
                val date = sdf.parse(dateStr)
                selectedDate = date?.let { Timestamp(it) }
                btnSelectDate.text = dateStr
            }
        }

        btnSubmitReport.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val period = etPeriod.text.toString().trim()
            val summary = etSummary.text.toString().trim()
            val reportType = spinnerReportType.selectedItem.toString()

            if (title.isEmpty() || period.isEmpty() || summary.isEmpty() || selectedDate == null) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveReportToFirestore(title, period, summary, reportType, selectedDate!!)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val dateStr = sdf.format(cal.time)
                onDateSelected(dateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveReportToFirestore(
        title: String,
        period: String,
        summary: String,
        reportType: String,
        date: Timestamp
    ) {
        val reportId = firestore.collection("reports").document().id

        val report = Report(
            title = title,
            period = period,
            summary = summary,
            type = reportType,
            date = date
        )

        firestore.collection("reports")
            .document(reportId)
            .set(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report added successfully", Toast.LENGTH_SHORT).show()
                fetchReports(reportType, "2000-01-01", sdf.format(Date()))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun validateInputs(): Boolean {
        if (selectedReportType.isBlank()) {
            Toast.makeText(this, "Please select report type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedStartDate.isBlank()) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedEndDate.isBlank()) {
            Toast.makeText(this, "Please select end date", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateChartData(reports: List<Report>) {
        if (reports.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        val groupedByDate = reports.groupBy { sdf.format(it.date!!.toDate()) }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        groupedByDate.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            labels.add(entry.key)
            entries.add(BarEntry(index.toFloat(), entry.value.size.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Reports Count by Date").apply {
            color = resources.getColor(R.color.primary_yellow, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            valueTextSize = 12f
        }

        val data = BarData(dataSet)
        data.barWidth = 0.4f

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.labelRotationAngle = -45f
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        barChart.invalidate()
    }



    private fun fetchReports(reportType: String, startDate: String, endDate: String) {

        val start: Date
        val end: Date
        try {
            start = sdf.parse(startDate)!!
            end = sdf.parse(endDate)!!
        } catch (e: ParseException) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("reports")
            .whereEqualTo("type", reportType)
            .whereGreaterThanOrEqualTo("date", Timestamp(start))
            .whereLessThanOrEqualTo("date", Timestamp(end))
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reportsList = mutableListOf<Report>()
                for (doc in querySnapshot.documents) {
                    val report = doc.toObject(Report::class.java)
                    if (report != null) {
                        reportsList.add(report)
                    }
                }

                if (reportsList.isEmpty()) {
                    rvReportData.visibility = View.GONE
                    tvNoData.visibility = View.VISIBLE
                    barChart.clear()
                    barChart.invalidate()
                } else {
                    rvReportData.visibility = View.VISIBLE
                    tvNoData.visibility = View.GONE
                    reportsAdapter.updateReports(reportsList)
                    updateChartData(reportsList)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch reports: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                rvReportData.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
                barChart.clear()
                barChart.invalidate()
            }
    }
}
