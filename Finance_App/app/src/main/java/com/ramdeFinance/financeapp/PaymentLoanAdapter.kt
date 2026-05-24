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
        holder.plan.text =
            "Plan: ${loan.paymentFrequency} | Payment: $${loan.paymentAmount}"
        holder.balance.text = "Remaining Balance: $${loan.remainingBalance}"

        holder.payButton.setOnClickListener {
            val paymentText = holder.payAmount.text.toString().trim()
            val paymentValue = paymentText.toDoubleOrNull()
            val currentBalance = loan.remainingBalance.toDoubleOrNull() ?: 0.0

            if (paymentValue == null || paymentValue <= 0.0) {
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

                        userRef.update(
                            "creditScore",
                            currentScore + 25
                        )
                    }
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
}