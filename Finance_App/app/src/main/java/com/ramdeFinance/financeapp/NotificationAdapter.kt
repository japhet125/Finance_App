package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val notificationList: List<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.txtNotificationTitle)
        val message: TextView = itemView.findViewById(R.id.txtNotificationMessage)
        val date: TextView = itemView.findViewById(R.id.txtNotificationDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)

        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]

        holder.title.text = notification.title
        holder.message.text = notification.message

        val formattedDate = SimpleDateFormat(
            "MMM dd, yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(notification.timestamp))

        holder.date.text = formattedDate
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}