package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PaymentLoanAdapter(
    private val loanList: List<Pair<String, PaymentLoanModel>>
) : RecyclerView.Adapter<PaymentLoanAdapter.PaymentLoanViewHolder>() {

    class PaymentLoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount: TextView = itemView.findViewById(R.id.txtPaymentLoanAmount)
        val reason: TextView = itemView.findViewById(R.id.txtPaymentLoanReason)
        val plan: TextView = itemView.findViewById(R.id.txtPaymentLoanPlan)
        val balance: TextView = itemView.findViewById(R.id.txtPaymentLoanBalance)
        val payAmount: EditText = itemView.findViewById(R.id.etPayAmount)
        val payButton: Button = itemView.findViewById(R.id.btnPayLoan)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentLoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_loan_item, parent, false)

        return PaymentLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentLoanViewHolder, position: Int) {
        val (documentId, loan) = loanList[position]

        holder.amount.text = "Original Loan: $${loan.amount}"
        holder.reason.text = "Reason: ${loan.reason}"
        val planText =
            if (loan.paymentFrequency == "one_time") {
                "One-Time Payment"
            } else {
                "${loan.paymentFrequency} (${loan.paymentTerm} payments)"
            }

        holder.plan.text =
            "Plan: $planText | Payment: $${loan.paymentAmount}"
        holder.balance.text = "Remaining Balance: $${loan.remainingBalance}"

        holder.payButton.setOnClickListener {
            val paymentText = holder.payAmount.text.toString().trim()

            val paymentValue = parseMoney(paymentText)

            val currentBalance = parseMoney(
                loan.remainingBalance
            )

            if (paymentValue <= 0.0) {

                Toast.makeText(
                    holder.itemView.context,
                    "Enter a valid payment amount",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (paymentValue > currentBalance) {

                Toast.makeText(
                    holder.itemView.context,
                    "Payment cannot exceed remaining balance",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val newBalance = currentBalance - paymentValue

            val db = FirebaseFirestore.getInstance()

            val updates = hashMapOf<String, Any>(
                "remainingBalance" to String.format("%.2f", newBalance)
            )

            if (newBalance <= 0.0) {

                updates["remainingBalance"] = "0.00"
                updates["status"] = "paid"

                val userRef = db.collection("users")
                    .document(loan.userId)

                userRef.get()
                    .addOnSuccessListener { userDocument ->

                        val currentScore =
                            userDocument.getLong("creditScore") ?: 500

                        val completedLoans =
                            userDocument.getLong("completedLoans") ?: 0

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
                val completionNotification = hashMapOf(
                    "userId" to loan.userId,
                    "title" to "Loan Completed 🎉",
                    "message" to "Congratulations! Your loan has been fully repaid. Your borrower level may have improved.",
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false
                )

                db.collection("notifications")
                    .add(completionNotification)
            }

            db.collection("loan_requests")
                .document(documentId)
                .update(updates)
                .addOnSuccessListener {

                    val transaction = hashMapOf(
                        "userId" to loan.userId,
                        "loanId" to documentId,
                        "paymentAmount" to String.format("%.2f", paymentValue),
                        "previousBalance" to String.format("%.2f", currentBalance),
                        "newBalance" to String.format("%.2f", newBalance.coerceAtLeast(0.0)),
                        "paymentDate" to System.currentTimeMillis(),
                        "paymentType" to "loan_repayment"
                    )

                    db.collection("transactions")
                        .add(transaction)
                        .addOnSuccessListener {
                            Toast.makeText(
                                holder.itemView.context,
                                "Payment successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            holder.payAmount.text.clear()
                        }
                }
        }
    }

    override fun getItemCount(): Int {
        return loanList.size
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