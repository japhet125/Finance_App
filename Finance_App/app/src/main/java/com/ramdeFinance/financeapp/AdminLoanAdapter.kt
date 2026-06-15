package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoanAdapter(
    private val loanList: List<Pair<String, AdminLoanModel>>,
    private var language: String = "en"
) : RecyclerView.Adapter<AdminLoanAdapter.AdminLoanViewHolder>() {

    class AdminLoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amount: TextView = itemView.findViewById(R.id.txtAdminAmount)
        val reason: TextView = itemView.findViewById(R.id.txtAdminReason)
        val plan: TextView = itemView.findViewById(R.id.txtAdminPlan)
        val status: TextView = itemView.findViewById(R.id.txtAdminStatus)

        val approveButton: Button = itemView.findViewById(R.id.btnApprove)
        val rejectButton: Button = itemView.findViewById(R.id.btnReject)
        val recommendation: TextView =
            itemView.findViewById(R.id.txtRecommendation)
        val riskFlags: TextView =
            itemView.findViewById(R.id.txtRiskFlags)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminLoanViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_loan_item, parent, false)

        return AdminLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminLoanViewHolder, position: Int) {

        val (documentId, loan) = loanList[position]
        val db = FirebaseFirestore.getInstance()
        val planText =
            if (language == "fr") {
                when (loan.paymentFrequency) {
                    "one_time" -> "Paiement unique"
                    "weekly" -> "Hebdomadaire (${loan.paymentTerm} paiements)"
                    "monthly" -> "Mensuel (${loan.paymentTerm} paiements)"
                    else -> loan.paymentFrequency
                }
            } else {
                if (loan.paymentFrequency == "one_time") {
                    "One-Time Payment"
                } else {
                    "${loan.paymentFrequency} (${loan.paymentTerm} payments)"
                }
            }

        if (language == "fr") {
            holder.amount.text = "Montant : ${loan.amount}"
            holder.reason.text = "Raison : ${loan.reason}"
            holder.plan.text = "Plan : $planText"
            holder.status.text = "Statut : ${translateStatus(loan.status)}"
            holder.approveButton.text = "Approuver"
            holder.rejectButton.text = "Rejeter"
            holder.recommendation.text = "Recommandation : Chargement..."
        } else {
            holder.amount.text = "Amount: $${loan.amount}"
            holder.reason.text = "Reason: ${loan.reason}"
            holder.plan.text = "Plan: $planText"
            holder.status.text = "Status: ${loan.status}"
            holder.approveButton.text = "Approve"
            holder.rejectButton.text = "Reject"
            holder.recommendation.text = "Recommendation: Loading..."
        }





        db.collection("users")
            .document(loan.userId)
            .get()
            .addOnSuccessListener { userDocument ->

                val creditScore =
                    userDocument.getLong("creditScore") ?: 500
                val borrowerLevel =
                    userDocument.getString("borrowerLevel") ?: "New"

                val identityVerified =
                    userDocument.getBoolean("identityVerified") ?: false
                val accountFlagged =
                    userDocument.getBoolean("accountFlagged") ?: false

                val flagReason =
                    userDocument.getString("flagReason") ?: ""


                val completedLoans =
                    userDocument.getLong("completedLoans") ?: 0

                val riskFlags = mutableListOf<String>()
                if (accountFlagged) {
                    riskFlags.add(
                        if (language == "fr")
                            "🚩 Utilisateur signalé : $flagReason"
                        else
                            "🚩 Flagged User: $flagReason"
                    )
                }

                if (!identityVerified) {
                    riskFlags.add(
                        if (language == "fr")
                            "⚠️ Identité non vérifiée"
                        else
                            "⚠️ Identity not verified"
                    )
                }

                if (completedLoans == 0L) {
                    riskFlags.add(
                        if (language == "fr")
                            "⚠️ Nouvel emprunteur"
                        else
                            "⚠️ New borrower"
                    )
                }

                if (borrowerLevel == "New") {
                    riskFlags.add(
                        if (language == "fr")
                            "ℹ️ Niveau emprunteur : Nouveau"
                        else
                            "ℹ️ Borrower level: New"
                    )
                }

                holder.riskFlags.text =
                    if (riskFlags.isEmpty()) {
                        if (language == "fr") {
                            "✅ Aucun signal de risque majeur"
                        } else {
                            "✅ No major risk flags"
                        }
                    } else {
                        riskFlags.joinToString("\n")
                    }



                val recommendationText =
                    if (language == "fr") {
                        when {
                            creditScore >= 700 ->
                                "✅ Recommandation : Approuver — bon score de crédit"

                            creditScore >= 550 ->
                                "⚠️ Recommandation : Réviser — risque moyen"

                            else ->
                                "❌ Recommandation : Rejeter — risque élevé"
                        }
                    } else {
                        when {
                            creditScore >= 700 ->
                                "✅ Recommendation: Approve — strong credit score"

                            creditScore >= 550 ->
                                "⚠️ Recommendation: Review — medium risk borrower"

                            else ->
                                "❌ Recommendation: Reject — high risk borrower"
                        }
                    }

                holder.recommendation.text = recommendationText
            }.addOnFailureListener { e ->
                holder.recommendation.text = "Recommendation unavailable: ${e.message}"
            }

        if (loan.status == "approved") {
            holder.approveButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.riskFlags.visibility = View.GONE
            holder.status.text =
                if (language == "fr") "✓ Approuvé" else "✓ Approved"

        } else if (loan.status == "rejected") {
            holder.approveButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.riskFlags.visibility = View.GONE
            holder.status.text =
                if (language == "fr") "✗ Rejeté" else "✗ Rejected"

        } else {
            holder.approveButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE
            holder.status.text =
                if (language == "fr") "Statut : En attente" else "Status: Pending"
        }




        holder.approveButton.setOnClickListener {

            val millisecondsPerDay =
                24L * 60L * 60L * 1000L

            val approvedAt =
                System.currentTimeMillis()

            val nextPaymentDate =
                when (loan.paymentFrequency) {
                    "weekly" -> approvedAt + (7L * millisecondsPerDay)
                    "monthly" -> approvedAt + (30L * millisecondsPerDay)
                    "one_time" -> approvedAt + (30L * millisecondsPerDay)
                    else -> approvedAt
                }

            val approvalUpdates = hashMapOf<String, Any>(
                "status" to "approved",
                "approvedAt" to approvedAt,
                "reminderSent" to false
            )

            if (loan.autoPayEnabled) {
                approvalUpdates["autoPayStatus"] = "scheduled"
                approvalUpdates["nextPaymentDate"] = nextPaymentDate
                approvalUpdates["nextPaymentAmount"] = loan.paymentAmount
            }

            db.collection("loan_requests")
                .document(documentId)
                .update(approvalUpdates)
                .addOnSuccessListener {

                    val notification = hashMapOf(
                        "userId" to loan.userId,
                        "title" to "Loan Approved",
                        "message" to "Your loan request for $${loan.amount} was approved.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications")
                        .add(notification)

                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "loan_approved",
                        "targetType" to "loan_request",
                        "targetId" to documentId,
                        "message" to "Loan $documentId was approved.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs")
                        .add(auditLog)
                }
        }

        holder.rejectButton.setOnClickListener {

            db.collection("loan_requests")
                .document(documentId)
                .update("status", "rejected")
                .addOnSuccessListener {

                    val notification = hashMapOf(
                        "userId" to loan.userId,
                        "title" to "Loan Rejected",
                        "message" to "Your loan request for $${loan.amount} was rejected.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                    db.collection("notifications")
                        .add(notification)

                    val auditLog = hashMapOf(
                        "actorId" to "admin",
                        "action" to "loan_rejected",
                        "targetType" to "loan_request",
                        "targetId" to documentId,
                        "message" to "Loan $documentId was rejected.",
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("audit_logs")
                        .add(auditLog)
                }
        }


    }


    override fun getItemCount(): Int {
        return loanList.size
    }
    private fun translateStatus(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "En attente"
            "approved" -> "Approuvé"
            "rejected" -> "Rejeté"
            "overdue" -> "En retard"
            "paid" -> "Payé"
            else -> status
        }
    }
    fun updateLanguage(newLanguage: String) {
        language = newLanguage
        notifyDataSetChanged()
    }
}