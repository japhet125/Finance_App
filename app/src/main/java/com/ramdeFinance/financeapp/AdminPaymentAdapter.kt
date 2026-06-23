package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminPaymentAdapter(
    private val paymentList: List<Pair<String, AdminPaymentModel>>
) : RecyclerView.Adapter<AdminPaymentAdapter.AdminPaymentViewHolder>() {

    class AdminPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userId: TextView = itemView.findViewById(R.id.txtPaymentUserId)
        val loanId: TextView = itemView.findViewById(R.id.txtPaymentLoanId)
        val amount: TextView = itemView.findViewById(R.id.txtPaymentAmount)
        val balance: TextView = itemView.findViewById(R.id.txtPaymentBalance)
        val date: TextView = itemView.findViewById(R.id.txtPaymentDate)
        val approveButton: Button = itemView.findViewById(R.id.btnApprovePayment)
        val rejectButton: Button = itemView.findViewById(R.id.btnRejectPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminPaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_payment_item, parent, false)

        return AdminPaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminPaymentViewHolder, position: Int) {
        val (transactionId, payment) = paymentList[position]
        val db = FirebaseFirestore.getInstance()

        holder.userId.text = "Customer: Loading..."
        holder.loanId.text = "Loan ID: ${payment.loanId}"
        holder.amount.text = "Payment Amount: $${payment.paymentAmount}"
        holder.balance.text =
            "Balance: $${payment.previousBalance} → $${payment.newBalance}"

        db.collection("users")
            .document(payment.userId)
            .get()
            .addOnSuccessListener { userDocument ->
                val fullName = userDocument.getString("fullName") ?: "Unknown Customer"
                val email = userDocument.getString("email") ?: "No email"

                holder.userId.text = "Customer: $fullName\nEmail: $email"
            }

        val dateText = java.text.SimpleDateFormat(
            "MMM dd, yyyy hh:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date(payment.paymentDate))

        holder.date.text = "Submitted: $dateText"

        holder.approveButton.setOnClickListener {
            val paymentAmount = parseMoney(payment.paymentAmount)
            val newBalance = parseMoney(payment.newBalance)

            val loanUpdates = hashMapOf<String, Any>(
                "remainingBalance" to String.format("%.2f", newBalance)
            )

            if (newBalance <= 0.0) {
                loanUpdates["remainingBalance"] = "0.00"
                loanUpdates["status"] = "paid"
            }

            db.collection("loan_requests")
                .document(payment.loanId)
                .update(loanUpdates)
                .addOnSuccessListener {

                    db.collection("transactions")
                        .document(transactionId)
                        .update(
                            mapOf(
                                "status" to "approved",
                                "approvedAt" to System.currentTimeMillis()
                            )
                        )

                    if (newBalance <= 0.0) {
                        updateBorrowerAfterLoanPaid(payment.userId, db)
                    }

                    val userRef = db.collection("users").document(payment.userId)

                    userRef.get().addOnSuccessListener { userDocument ->
                        val userLanguage = userDocument.getString("language") ?: "en"
                        val fullName = userDocument.getString("fullName") ?: "Customer"
                        val email = userDocument.getString("email") ?: ""

                        val notification = hashMapOf(
                            "userId" to payment.userId,
                            "title" to if (userLanguage == "fr") {
                                "Paiement publié ✅"
                            } else {
                                "Payment Posted ✅"
                            },
                            "message" to if (userLanguage == "fr") {
                                "Votre paiement de $${String.format("%.2f", paymentAmount)} a été publié. Le solde de votre prêt a été mis à jour."
                            } else {
                                "Your payment of $${String.format("%.2f", paymentAmount)} has been posted. Your loan balance has been updated."
                            },
                            "timestamp" to System.currentTimeMillis(),
                            "isRead" to false
                        )

                        db.collection("notifications").add(notification)

                        val emailSubject = if (userLanguage == "fr") {
                            "Paiement enregistré avec succès"
                        } else {
                            "Payment Posted Successfully"
                        }

                        val emailMessage = if (userLanguage == "fr") {
                            """
        Bonjour $fullName,

        Votre paiement de $${String.format("%.2f", paymentAmount)} a été approuvé et appliqué au solde de votre prêt.

        Merci d'avoir choisi Baobab Finance.

        L'équipe Baobab
        """.trimIndent()
                        } else {
                            """
        Hello $fullName,

        Your payment of $${String.format("%.2f", paymentAmount)} has been approved and applied to your loan balance.

        Thank you for choosing Baobab Finance.

        Baobab Team
        """.trimIndent()
                        }

                        val emailRequest = hashMapOf(
                            "userId" to payment.userId,
                            "email" to email,
                            "type" to "payment_approved",
                            "subject" to emailSubject,
                            "message" to emailMessage,
                            "status" to "pending",
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("email_requests").add(emailRequest)
                    }

                    Toast.makeText(
                        holder.itemView.context,
                        "Payment approved and posted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Approval failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        holder.rejectButton.setOnClickListener {
            val paymentAmount = parseMoney(payment.paymentAmount)
            db.collection("transactions")
                .document(transactionId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "rejectedAt" to System.currentTimeMillis()
                    )
                )
                .addOnSuccessListener {

                    val userRef = db.collection("users").document(payment.userId)

                    userRef.get().addOnSuccessListener { userDocument ->
                        val userLanguage = userDocument.getString("language") ?: "en"
                        val fullName = userDocument.getString("fullName") ?: "Customer"
                        val email = userDocument.getString("email") ?: ""

                        val notification = hashMapOf(
                            "userId" to payment.userId,
                            "title" to if (userLanguage == "fr") {
                                "Paiement rejeté"
                            } else {
                                "Payment Rejected"
                            },
                            "message" to if (userLanguage == "fr") {
                                "Votre paiement de $${String.format("%.2f", paymentAmount)} a été rejeté. Contactez votre service clientel."
                            } else {
                                "Your payment of $${String.format("%.2f", paymentAmount)} has been rejected. Please contact your customers support."
                            },
                            "timestamp" to System.currentTimeMillis(),
                            "isRead" to false
                        )

                        db.collection("notifications").add(notification)

                        val emailSubject = if (userLanguage == "fr") {
                            "Paiement rejeté"
                        } else {
                            "Payment rejected"
                        }

                        val emailMessage = if (userLanguage == "fr") {
                            """
        Bonjour $fullName,

        Votre paiement de $${String.format("%.2f", paymentAmount)} a été rejeté contacter votre service clientel.

        Merci d'avoir choisi Baobab Finance.

        L'équipe Baobab
        """.trimIndent()
                        } else {
                            """
        Hello $fullName,

        Your payment of $${String.format("%.2f", paymentAmount)} has been rejected please contact your support.

        Thank you for choosing Baobab Finance.

        Baobab Team
        """.trimIndent()
                        }

                        val emailRequest = hashMapOf(
                            "userId" to payment.userId,
                            "fullName" to fullName,
                            "email" to email,
                            "type" to "payment_approved",
                            "subject" to emailSubject,
                            "message" to emailMessage,
                            "status" to "pending",
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("email_requests").add(emailRequest)
                    }

                    Toast.makeText(
                        holder.itemView.context,
                        "Payment rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }

    private fun updateBorrowerAfterLoanPaid(userId: String, db: FirebaseFirestore) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { userDocument ->

                val currentScore = userDocument.getLong("creditScore") ?: 500
                val completedLoans = userDocument.getLong("completedLoans") ?: 0
                val newCompletedLoans = completedLoans + 1

                val borrowerLevel = when {
                    newCompletedLoans >= 15 -> "Platinum"
                    newCompletedLoans >= 8 -> "Gold"
                    newCompletedLoans >= 5 -> "Silver"
                    newCompletedLoans >= 3 -> "Bronze"
                    else -> "New"
                }

                userRef.update(
                    mapOf(
                        "creditScore" to currentScore + 25,
                        "completedLoans" to newCompletedLoans,
                        "borrowerLevel" to borrowerLevel
                    )
                )
            }
    }

    private fun parseMoney(value: String): Double {
        return value
            .replace("$", "")
            .replace("FCFA", "")
            .replace("F CFA", "")
            .replace("CFA", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }
}