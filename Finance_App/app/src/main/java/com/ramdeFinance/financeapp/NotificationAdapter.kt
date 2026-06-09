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
    private val notificationList: List<NotificationModel>,
    private var language: String = "en"
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

        if (language == "fr") {
            holder.title.text = translateTitle(notification.title)
            holder.message.text = translateMessage(notification.message)
        } else {
            holder.title.text = notification.title
            holder.message.text = notification.message
        }

        val formattedDate = SimpleDateFormat(
            "MMM dd, yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(notification.timestamp))

        holder.date.text = formattedDate
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
    fun updateLanguage(newLanguage: String) {
        language = newLanguage
        notifyDataSetChanged()
    }
    private fun translateTitle(title: String): String {
        return when (title) {
            "Loan Approved" -> "Prêt approuvé"
            "Loan Rejected" -> "Prêt rejeté"
            "Loan Overdue" -> "Prêt en retard"
            "Auto Pay Processed" -> "Paiement automatique traité"
            "Payment Reminder" -> "Rappel de paiement"
            "Loan Completed 🎉" -> "Prêt terminé 🎉"
            "Account Suspended" -> "Compte suspendu"
            "Account Reactivated" -> "Compte réactivé"
            "Identity Verified" -> "Identité vérifiée"
            "Identity Rejected" -> "Identité rejetée"
            else -> title
        }
    }
    private fun translateMessage(message: String): String {
        return when {
            message.contains("loan request", ignoreCase = true) &&
                    message.contains("approved", ignoreCase = true) ->
                "Votre demande de prêt a été approuvée."

            message.contains("loan request", ignoreCase = true) &&
                    message.contains("rejected", ignoreCase = true) ->
                "Votre demande de prêt a été rejetée."

            message.contains("overdue", ignoreCase = true) ->
                "Votre prêt est en retard. Veuillez effectuer un paiement dès que possible."

            message.contains("automatic payment", ignoreCase = true) ->
                "Votre paiement automatique a été traité."

            message.contains("due tomorrow", ignoreCase = true) ->
                "Votre paiement est dû demain."

            message.contains("fully repaid", ignoreCase = true) ->
                "Félicitations ! Votre prêt a été entièrement remboursé."

            else -> message
        }
    }
}