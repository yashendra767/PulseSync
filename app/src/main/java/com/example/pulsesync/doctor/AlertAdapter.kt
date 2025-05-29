package com.example.pulsesync.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.android.material.card.MaterialCardView

class AlertAdapter(private val alerts: List<AlertItem>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textAlertMessage)
        val description : TextView = itemView.findViewById(R.id.textAlertDescription)
        val cardView: MaterialCardView = itemView.findViewById(R.id.alertCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alert_item, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.title.text = alert.title
        holder.description.text = alert.description
        val context = holder.itemView.context
        when (alert.severity) {
            "High" -> holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.severity_high))
            "Medium" -> holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.severity_medium))
            "Low" -> holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.severity_low))
        }
    }

    override fun getItemCount(): Int = alerts.size
}
