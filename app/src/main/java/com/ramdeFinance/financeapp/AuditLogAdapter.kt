package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditLogAdapter(
    private val auditLogs: List<AuditLogModel>
) : RecyclerView.Adapter<AuditLogAdapter.AuditLogViewHolder>() {

    class AuditLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val action: TextView = itemView.findViewById(R.id.txtAuditAction)
        val message: TextView = itemView.findViewById(R.id.txtAuditMessage)
        val target: TextView = itemView.findViewById(R.id.txtAuditTarget)
        val date: TextView = itemView.findViewById(R.id.txtAuditDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.audit_log_item, parent, false)

        return AuditLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditLogViewHolder, position: Int) {
        val log = auditLogs[position]

        holder.action.text = "Action: ${log.action}"
        holder.message.text = log.message
        holder.target.text = "Target: ${log.targetType} / ${log.targetId}"

        val formattedDate = SimpleDateFormat(
            "MMM dd, yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(log.timestamp))

        holder.date.text = "Date: $formattedDate"
    }

    override fun getItemCount(): Int {
        return auditLogs.size
    }
}