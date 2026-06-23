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
import android.text.Editable
import android.text.TextWatcher

class PaymentLoanAdapter(
    private val loanList: List<Pair<String, PaymentLoanModel>>,
    private var language: String = "en"
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
            holder.amount.text = "Prêt initial : ${loan.amount}"
            holder.reason.text = "Raison : ${loan.reason}"
            holder.plan.text = "Plan : $planText | Paiement : ${loan.paymentAmount}"
            holder.balance.text = "Solde restant : ${loan.remainingBalance}"
            holder.payAmount.hint = "Montant du paiement"
            holder.payButton.text = "Payer"
        } else {
            holder.amount.text = "Original Loan: $${loan.amount}"
            holder.reason.text = "Reason: ${loan.reason}"
            holder.plan.text = "Plan: $planText | Payment: $${loan.paymentAmount}"
            holder.balance.text = "Remaining Balance: $${loan.remainingBalance}"
            holder.payAmount.hint = "Payment Amount"
            holder.payButton.text = "Pay"
        }

        val currentBalance = parseMoney(loan.remainingBalance)
        if (loan.status == "paid" || currentBalance <= 0.0) {
            holder.payButton.isEnabled = false
            holder.payButton.alpha = 0.5f
            holder.payAmount.isEnabled = false

            holder.payButton.text =
                if (language == "fr") "Payé" else "Paid"

            return
        }

        if (loan.paymentFrequency == "one_time") {
            holder.payButton.isEnabled = false
            holder.payButton.alpha = 0.5f

            holder.payAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val enteredAmount = parseMoney(s.toString())

                    if (enteredAmount >= currentBalance) {
                        holder.payButton.isEnabled = true
                        holder.payButton.alpha = 1.0f
                    } else {
                        holder.payButton.isEnabled = false
                        holder.payButton.alpha = 0.5f
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        } else {
            holder.payButton.isEnabled = true
            holder.payButton.alpha = 1.0f
        }


        holder.payButton.setOnClickListener {
            holder.payButton.isEnabled = false
            holder.payButton.alpha = 0.5f
            val paymentText = holder.payAmount.text.toString().trim()

            val paymentValue = parseMoney(paymentText)

            val currentBalance = parseMoney(
                loan.remainingBalance
            )

            if (paymentValue <= 0.0) {

                Toast.makeText(
                    holder.itemView.context,
                    if (language == "fr") {
                        "Entrez un montant de paiement valide"
                    } else {
                        "Enter a valid payment amount"
                    },
                    Toast.LENGTH_SHORT
                ).show()
                holder.payButton.isEnabled = true
                holder.payButton.alpha = 1.0f

                return@setOnClickListener
            }

            if (paymentValue > currentBalance) {

                Toast.makeText(
                    holder.itemView.context,
                    if (language == "fr") {
                        "Le paiement ne peut pas dépasser le solde restant"
                    } else {
                        "Payment cannot exceed remaining balance"
                    },
                    Toast.LENGTH_SHORT
                ).show()
                holder.payButton.isEnabled = true
                holder.payButton.alpha = 1.0f

                return@setOnClickListener
            }
            if (loan.paymentFrequency == "one_time" && paymentValue < currentBalance) {
                Toast.makeText(
                    holder.itemView.context,
                    if (language == "fr") {
                        "Le paiement unique nécessite le montant total dû : $${
                            String.format(
                                "%.2f",
                                currentBalance
                            )
                        }"
                    } else {
                        "One-time payment requires the full amount due: $${
                            String.format(
                                "%.2f",
                                currentBalance
                            )
                        }"
                    },
                    Toast.LENGTH_LONG
                ).show()
                holder.payButton.isEnabled = true
                holder.payButton.alpha = 1.0f

                return@setOnClickListener
            }
            val newBalance = currentBalance - paymentValue

            val db = FirebaseFirestore.getInstance()

            val pendingPayment = hashMapOf(
                "userId" to loan.userId,
                "loanId" to documentId,
                "paymentAmount" to String.format("%.2f", paymentValue),
                "previousBalance" to String.format("%.2f", currentBalance),
                "newBalance" to String.format("%.2f", newBalance.coerceAtLeast(0.0)),
                "paymentDate" to System.currentTimeMillis(),
                "paymentType" to "loan_repayment",
                "status" to "pending"
            )


            db.collection("transactions")
                .whereEqualTo("loanId", documentId)
                .whereEqualTo("userId", loan.userId)
                .whereEqualTo("status", "pending")
                .whereEqualTo("paymentType", "loan_repayment")
                .get()
                .addOnSuccessListener { pendingPayments ->

                    if (!pendingPayments.isEmpty) {

                        Toast.makeText(
                            holder.itemView.context,
                            if (language == "fr")
                                "Vous avez déjà un paiement en attente pour ce prêt."
                            else
                                "You already have a pending payment for this loan.",
                            Toast.LENGTH_LONG
                        ).show()

                        holder.payButton.isEnabled = false
                        holder.payButton.alpha = 0.5f

                        return@addOnSuccessListener
                    }

                    db.collection("transactions")
                        .add(pendingPayment)
                        .addOnSuccessListener {
                            holder.payButton.isEnabled = false
                            holder.payButton.alpha = 0.5f

                            holder.payButton.text =
                                if (language == "fr") "Soumis" else "Submitted"

                            Toast.makeText(
                                holder.itemView.context,
                                if (language == "fr")
                                    "Paiement soumis pour approbation."
                                else
                                    "Payment submitted for admin review.",
                                Toast.LENGTH_LONG
                            ).show()

                            holder.payAmount.text.clear()
                        }
                        .addOnFailureListener { e ->

                            holder.payButton.isEnabled = true
                            holder.payButton.alpha = 1.0f

                            Toast.makeText(
                                holder.itemView.context,
                                "Payment failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
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

        fun updateLanguage(newLanguage: String) {
            language = newLanguage
            notifyDataSetChanged()
        }
    }
