package com.ramdefinance.financeapp

import android.content.Context
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

    class AuditLogViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val action: TextView =
            itemView.findViewById(R.id.txtAuditAction)

        val message: TextView =
            itemView.findViewById(R.id.txtAuditMessage)

        val target: TextView =
            itemView.findViewById(R.id.txtAuditTarget)

        val date: TextView =
            itemView.findViewById(R.id.txtAuditDate)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AuditLogViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.audit_log_item,
                parent,
                false
            )

        return AuditLogViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AuditLogViewHolder,
        position: Int
    ) {
        val log = auditLogs[position]
        val context = holder.itemView.context

        val translatedAction =
            translateAction(
                context = context,
                action = log.action
            )

        val translatedTargetType =
            translateTargetType(
                context = context,
                targetType = log.targetType
            )

        holder.action.text =
            context.getString(
                R.string.audit_action,
                translatedAction
            )

        holder.message.text =
            translateMessage(
                context = context,
                message = log.message
            )

        holder.target.text =
            context.getString(
                R.string.audit_target,
                translatedTargetType,
                log.targetId
            )

        val locale =
            context.resources.configuration.locales[0]

        val formattedDate =
            SimpleDateFormat(
                "MMM dd, yyyy HH:mm",
                locale
            ).format(
                Date(log.timestamp)
            )

        holder.date.text =
            context.getString(
                R.string.audit_date,
                formattedDate
            )
    }

    override fun getItemCount(): Int {
        return auditLogs.size
    }

    private fun translateAction(
        context: Context,
        action: String
    ): String {
        return when (action.lowercase()) {
            "identity_verified" ->
                context.getString(
                    R.string.audit_action_identity_verified
                )

            "identity_rejected" ->
                context.getString(
                    R.string.audit_action_identity_rejected
                )

            "loan_approved" ->
                context.getString(
                    R.string.audit_action_loan_approved
                )

            "loan_rejected" ->
                context.getString(
                    R.string.audit_action_loan_rejected
                )

            "payment_approved" ->
                context.getString(
                    R.string.audit_action_payment_approved
                )

            "payment_rejected" ->
                context.getString(
                    R.string.audit_action_payment_rejected
                )

            else ->
                action
                    .replace("_", " ")
                    .replaceFirstChar { character ->
                        character.uppercase()
                    }
        }
    }

    private fun translateTargetType(
        context: Context,
        targetType: String
    ): String {
        return when (targetType.lowercase()) {
            "user" ->
                context.getString(
                    R.string.audit_target_user
                )

            "loan" ->
                context.getString(
                    R.string.audit_target_loan
                )

            "payment" ->
                context.getString(
                    R.string.audit_target_payment
                )

            "transaction" ->
                context.getString(
                    R.string.audit_target_transaction
                )

            else ->
                targetType
        }
    }

    private fun translateMessage(
        context: Context,
        message: String
    ): String {
        return when (message.trim()) {
            "User identity was verified." ->
                context.getString(
                    R.string.audit_message_identity_verified
                )

            "User identity was rejected." ->
                context.getString(
                    R.string.audit_message_identity_rejected
                )

            else ->
                message
        }
    }
}