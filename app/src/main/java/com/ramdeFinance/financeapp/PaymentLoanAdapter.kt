package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PaymentLoanAdapter(
    private val loanList: List<Pair<String, PaymentLoanModel>>,
    private var language: String = "en"
) : RecyclerView.Adapter<PaymentLoanAdapter.PaymentLoanViewHolder>() {

    class PaymentLoanViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val amount: TextView =
            itemView.findViewById(R.id.txtPaymentLoanAmount)

        val reason: TextView =
            itemView.findViewById(R.id.txtPaymentLoanReason)

        val plan: TextView =
            itemView.findViewById(R.id.txtPaymentLoanPlan)

        val balance: TextView =
            itemView.findViewById(R.id.txtPaymentLoanBalance)

        val payAmount: TextInputEditText =
            itemView.findViewById(R.id.etPayAmount)

        val payButton: MaterialButton =
            itemView.findViewById(R.id.btnPayLoan)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentLoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_loan_item, parent, false)

        return PaymentLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentLoanViewHolder, position: Int) {
        val (documentId, loan) = loanList[position]
        val formattedLoanAmount =
            CurrencyFormatter.format(
                amount = parseMoney(loan.amount),
                currencyCode = "XOF",
                languageCode = language
            )

        val formattedPaymentAmount =
            CurrencyFormatter.format(
                amount = parseMoney(loan.paymentAmount),
                currencyCode = "XOF",
                languageCode = language
            )

        val formattedRemainingBalance =
            CurrencyFormatter.format(
                amount = parseMoney(loan.remainingBalance),
                currencyCode = "XOF",
                languageCode = language
            )
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
            holder.amount.text =
                "Prêt initial : $formattedLoanAmount"

            holder.reason.text =
                "Raison : ${loan.reason}"

            holder.plan.text =
                "Plan : $planText | Paiement : $formattedPaymentAmount"

            holder.balance.text =
                "Solde restant : $formattedRemainingBalance"

            holder.payButton.text =
                "Payer"
        } else {
            holder.amount.text =
                "Original Loan: $formattedLoanAmount"

            holder.reason.text =
                "Reason: ${loan.reason}"

            holder.plan.text =
                "Plan: $planText | Payment: $formattedPaymentAmount"

            holder.balance.text =
                "Remaining Balance: $formattedRemainingBalance"

            holder.payButton.text =
                "Pay"
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

            val paymentValue =
                String.format("%.2f", parseMoney(paymentText)).toDouble()

            val currentBalance =
                String.format("%.2f", parseMoney(loan.remainingBalance)).toDouble()

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
            val formattedCurrentBalance =
                CurrencyFormatter.format(
                    amount = currentBalance,
                    currencyCode = "XOF",
                    languageCode = language
                )
            if (loan.paymentFrequency == "one_time" && paymentValue < currentBalance) {
                Toast.makeText(
                    holder.itemView.context,
                    if (language == "fr") {
                        "Le paiement unique nécessite le montant total dû : $formattedCurrentBalance"
                    } else {
                        "One-time payment requires the full amount due: $formattedCurrentBalance"
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
                                if (language == "fr") {
                                    "Paiement soumis pour approbation."
                                } else {
                                    "Payment submitted for admin review."
                                },
                                Toast.LENGTH_LONG
                            ).show()

                            holder.payAmount.text?.clear()
                        }
                        .addOnFailureListener { e ->

                            holder.payButton.isEnabled = true
                            holder.payButton.alpha = 1.0f

                            Toast.makeText(
                                holder.itemView.context,
                                if (language == "fr")
                                    "Échec du paiement : ${e.message}"
                                else
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
