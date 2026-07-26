package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminPaymentAdapter(
    private val paymentList: List<Pair<String, AdminPaymentModel>>
) : RecyclerView.Adapter<AdminPaymentAdapter.AdminPaymentViewHolder>() {

    class AdminPaymentViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val userId: TextView =
            itemView.findViewById(R.id.txtPaymentUserId)

        val loanId: TextView =
            itemView.findViewById(R.id.txtPaymentLoanId)

        val amount: TextView =
            itemView.findViewById(R.id.txtPaymentAmount)

        val balance: TextView =
            itemView.findViewById(R.id.txtPaymentBalance)

        val date: TextView =
            itemView.findViewById(R.id.txtPaymentDate)

        val approveButton: Button =
            itemView.findViewById(R.id.btnApprovePayment)

        val rejectButton: Button =
            itemView.findViewById(R.id.btnRejectPayment)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminPaymentViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.admin_payment_item,
                parent,
                false
            )

        return AdminPaymentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AdminPaymentViewHolder,
        position: Int
    ) {
        val (transactionId, payment) =
            paymentList[position]

        val db =
            FirebaseFirestore.getInstance()

        val paymentAmount =
            parseMoney(payment.paymentAmount)

        val previousBalance =
            parseMoney(payment.previousBalance)

        val newBalance =
            parseMoney(payment.newBalance)

        val formattedPaymentAmount =
            formatCfa(paymentAmount)

        val formattedPreviousBalance =
            formatCfa(previousBalance)

        val formattedNewBalance =
            formatCfa(newBalance)

        holder.userId.text =
            "Customer: Loading..."

        holder.loanId.text =
            "Loan ID: ${payment.loanId}"

        holder.amount.text =
            "Payment Amount: $formattedPaymentAmount"

        holder.balance.text =
            "Balance: $formattedPreviousBalance → $formattedNewBalance"

        loadCustomerInformation(
            holder = holder,
            payment = payment,
            db = db
        )

        val dateText =
            SimpleDateFormat(
                "MMM dd, yyyy hh:mm a",
                Locale.getDefault()
            ).format(Date(payment.paymentDate))

        holder.date.text =
            "Submitted: $dateText"

        holder.approveButton.setOnClickListener {
            approvePayment(
                holder = holder,
                transactionId = transactionId,
                payment = payment,
                paymentAmount = paymentAmount,
                newBalance = newBalance,
                db = db
            )
        }

        holder.rejectButton.setOnClickListener {
            rejectPayment(
                holder = holder,
                transactionId = transactionId,
                payment = payment,
                paymentAmount = paymentAmount,
                db = db
            )
        }
    }

    private fun loadCustomerInformation(
        holder: AdminPaymentViewHolder,
        payment: AdminPaymentModel,
        db: FirebaseFirestore
    ) {
        db.collection("users")
            .document(payment.userId)
            .get()
            .addOnSuccessListener { userDocument ->
                val fullName =
                    userDocument.getString("fullName")
                        ?: "Unknown Customer"

                val email =
                    userDocument.getString("email")
                        ?: "No email"

                holder.userId.text =
                    "Customer: $fullName\nEmail: $email"
            }
            .addOnFailureListener {
                holder.userId.text =
                    "Customer: Unknown\nEmail: Unavailable"
            }
    }

    private fun approvePayment(
        holder: AdminPaymentViewHolder,
        transactionId: String,
        payment: AdminPaymentModel,
        paymentAmount: Double,
        newBalance: Double,
        db: FirebaseFirestore
    ) {
        val loanUpdates =
            hashMapOf<String, Any>(
                "remainingBalance" to formatStoredAmount(newBalance)
            )

        if (newBalance <= 0.0) {
            loanUpdates["remainingBalance"] = "0.00"
            loanUpdates["status"] = "paid"
        }

        db.collection("loan_requests")
            .document(payment.loanId)
            .update(loanUpdates)
            .addOnSuccessListener {
                approveTransaction(
                    transactionId = transactionId,
                    db = db
                )

                if (newBalance <= 0.0) {
                    updateBorrowerAfterLoanPaid(
                        userId = payment.userId,
                        db = db
                    )
                }

                sendApprovalMessages(
                    payment = payment,
                    paymentAmount = paymentAmount,
                    db = db
                )

                Toast.makeText(
                    holder.itemView.context,
                    "Payment approved and posted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    holder.itemView.context,
                    "Approval failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun approveTransaction(
        transactionId: String,
        db: FirebaseFirestore
    ) {
        db.collection("transactions")
            .document(transactionId)
            .update(
                mapOf(
                    "status" to "approved",
                    "approvedAt" to System.currentTimeMillis()
                )
            )
    }

    private fun sendApprovalMessages(
        payment: AdminPaymentModel,
        paymentAmount: Double,
        db: FirebaseFirestore
    ) {
        val userRef =
            db.collection("users")
                .document(payment.userId)

        userRef.get()
            .addOnSuccessListener { userDocument ->
                val userLanguage =
                    userDocument.getString("language")
                        ?: "en"

                val fullName =
                    userDocument.getString("fullName")
                        ?: "Customer"

                val email =
                    userDocument.getString("email")
                        ?: ""

                val formattedAmount =
                    formatCfa(paymentAmount)

                val notification =
                    hashMapOf(
                        "userId" to payment.userId,
                        "title" to if (userLanguage == "fr") {
                            "Paiement publié ✅"
                        } else {
                            "Payment Posted ✅"
                        },
                        "message" to if (userLanguage == "fr") {
                            "Votre paiement de $formattedAmount a été publié. " +
                                    "Le solde de votre prêt a été mis à jour."
                        } else {
                            "Your payment of $formattedAmount has been posted. " +
                                    "Your loan balance has been updated."
                        },
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                db.collection("notifications")
                    .add(notification)

                val emailSubject =
                    if (userLanguage == "fr") {
                        "Paiement enregistré avec succès"
                    } else {
                        "Payment Posted Successfully"
                    }

                val emailMessage =
                    if (userLanguage == "fr") {
                        """
                        Bonjour $fullName,

                        Votre paiement de $formattedAmount a été approuvé et appliqué au solde de votre prêt.

                        Merci d'avoir choisi Baobab Finance.

                        L'équipe Baobab
                        """.trimIndent()
                    } else {
                        """
                        Hello $fullName,

                        Your payment of $formattedAmount has been approved and applied to your loan balance.

                        Thank you for choosing Baobab Finance.

                        Baobab Team
                        """.trimIndent()
                    }

                val emailRequest =
                    hashMapOf(
                        "userId" to payment.userId,
                        "fullName" to fullName,
                        "email" to email,
                        "type" to "payment_approved",
                        "subject" to emailSubject,
                        "message" to emailMessage,
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )

                db.collection("email_requests")
                    .add(emailRequest)
            }
    }

    private fun rejectPayment(
        holder: AdminPaymentViewHolder,
        transactionId: String,
        payment: AdminPaymentModel,
        paymentAmount: Double,
        db: FirebaseFirestore
    ) {
        db.collection("transactions")
            .document(transactionId)
            .update(
                mapOf(
                    "status" to "rejected",
                    "rejectedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                sendRejectionMessages(
                    payment = payment,
                    paymentAmount = paymentAmount,
                    db = db
                )

                Toast.makeText(
                    holder.itemView.context,
                    "Payment rejected",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    holder.itemView.context,
                    "Rejection failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendRejectionMessages(
        payment: AdminPaymentModel,
        paymentAmount: Double,
        db: FirebaseFirestore
    ) {
        val userRef =
            db.collection("users")
                .document(payment.userId)

        userRef.get()
            .addOnSuccessListener { userDocument ->
                val userLanguage =
                    userDocument.getString("language")
                        ?: "en"

                val fullName =
                    userDocument.getString("fullName")
                        ?: "Customer"

                val email =
                    userDocument.getString("email")
                        ?: ""

                val formattedAmount =
                    formatCfa(paymentAmount)

                val notification =
                    hashMapOf(
                        "userId" to payment.userId,
                        "title" to if (userLanguage == "fr") {
                            "Paiement rejeté"
                        } else {
                            "Payment Rejected"
                        },
                        "message" to if (userLanguage == "fr") {
                            "Votre paiement de $formattedAmount a été rejeté. " +
                                    "Veuillez contacter le service client."
                        } else {
                            "Your payment of $formattedAmount has been rejected. " +
                                    "Please contact customer support."
                        },
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )

                db.collection("notifications")
                    .add(notification)

                val emailSubject =
                    if (userLanguage == "fr") {
                        "Paiement rejeté"
                    } else {
                        "Payment Rejected"
                    }

                val emailMessage =
                    if (userLanguage == "fr") {
                        """
                        Bonjour $fullName,

                        Votre paiement de $formattedAmount a été rejeté.

                        Veuillez contacter le service client pour obtenir de l'aide.

                        Merci d'avoir choisi Baobab Finance.

                        L'équipe Baobab
                        """.trimIndent()
                    } else {
                        """
                        Hello $fullName,

                        Your payment of $formattedAmount has been rejected.

                        Please contact customer support for assistance.

                        Thank you for choosing Baobab Finance.

                        Baobab Team
                        """.trimIndent()
                    }

                val emailRequest =
                    hashMapOf(
                        "userId" to payment.userId,
                        "fullName" to fullName,
                        "email" to email,
                        "type" to "payment_rejected",
                        "subject" to emailSubject,
                        "message" to emailMessage,
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )

                db.collection("email_requests")
                    .add(emailRequest)
            }
    }

    private fun updateBorrowerAfterLoanPaid(
        userId: String,
        db: FirebaseFirestore
    ) {
        val userRef =
            db.collection("users")
                .document(userId)

        userRef.get()
            .addOnSuccessListener { userDocument ->
                val currentScore =
                    userDocument.getLong("creditScore")
                        ?: 500

                val completedLoans =
                    userDocument.getLong("completedLoans")
                        ?: 0

                val newCompletedLoans =
                    completedLoans + 1

                val borrowerLevel =
                    when {
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

    private fun formatCfa(
        amount: Double
    ): String {
        val formatter =
            NumberFormat.getNumberInstance(Locale.FRANCE)

        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        return "${formatter.format(amount)} F CFA"
    }

    private fun formatStoredAmount(
        amount: Double
    ): String {
        return String.format(
            Locale.US,
            "%.2f",
            amount
        )
    }

    private fun parseMoney(
        value: String
    ): Double {
        val cleanedValue =
            value
                .replace("$", "")
                .replace("F CFA", "")
                .replace("FCFA", "")
                .replace("CFA", "")
                .replace("\u00A0", "")
                .replace("\u202F", "")
                .replace(" ", "")
                .trim()

        return when {
            cleanedValue.contains(",") &&
                    cleanedValue.contains(".") -> {
                cleanedValue
                    .replace(",", "")
                    .toDoubleOrNull()
                    ?: 0.0
            }

            cleanedValue.contains(",") -> {
                cleanedValue
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?: 0.0
            }

            else -> {
                cleanedValue.toDoubleOrNull()
                    ?: 0.0
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }
}