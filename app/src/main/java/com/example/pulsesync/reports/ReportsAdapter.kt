package com.example.pulsesync.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R

class ReportsAdapter(private var reports: List<Report>) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        val tvPeriod: TextView = itemView.findViewById(R.id.tvReportPeriod)
        val tvSummary: TextView = itemView.findViewById(R.id.tvReportSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_item, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.tvTitle.text = report.title
        holder.tvPeriod.text = report.period
        holder.tvSummary.text = report.summary
    }

    override fun getItemCount(): Int = reports.size

    fun updateReports(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
