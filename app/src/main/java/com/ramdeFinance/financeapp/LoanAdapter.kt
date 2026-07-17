package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoanAdapter(
    private val loanList: List<LoanModel>,
    private var language: String = "en"
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val amount: TextView =
            itemView.findViewById(R.id.txtAmount)

        val reason: TextView =
            itemView.findViewById(R.id.txtReason)

        val status: TextView =
            itemView.findViewById(R.id.txtStatus)

        val plan: TextView =
            itemView.findViewById(R.id.txtPlan)

        val interest: TextView =
            itemView.findViewById(R.id.txtInterest)

        val totalRepayment: TextView =
            itemView.findViewById(R.id.txtTotalRepayment)

        val paymentAmount: TextView =
            itemView.findViewById(R.id.txtPaymentAmount)

        val autoPayStatus: TextView =
            itemView.findViewById(R.id.txtAutoPayStatus)

        val nextPayment: TextView =
            itemView.findViewById(R.id.txtNextPayment)

        val remainingBalance: TextView =
            itemView.findViewById(R.id.txtRemainingBalance)

        val dueDate: TextView =
            itemView.findViewById(R.id.txtDueDate)

        val progressText: TextView =
            itemView.findViewById(R.id.txtLoanProgress)

        val progressBar: ProgressBar =
            itemView.findViewById(R.id.progressLoan)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoanViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.loan_item,
                parent,
                false
            )

        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LoanViewHolder,
        position: Int
    ) {
        val loan = loanList[position]

        val amountValue =
            parseMoney(loan.amount)

        val totalRepaymentValue =
            parseMoney(loan.totalRepayment)

        val paymentAmountValue =
            parseMoney(loan.paymentAmount)

        val remainingBalanceValue =
            parseMoney(loan.remainingBalance)

        val nextPaymentAmountValue =
            parseMoney(loan.nextPaymentAmount)

        val formattedAmount =
            CurrencyFormatter.format(
                amountValue,
                language
            )

        val formattedTotalRepayment =
            CurrencyFormatter.format(
                totalRepaymentValue,
                language
            )

        val formattedPaymentAmount =
            CurrencyFormatter.format(
                paymentAmountValue,
                language
            )

        val formattedRemainingBalance =
            CurrencyFormatter.format(
                remainingBalanceValue,
                language
            )

        val formattedNextPaymentAmount =
            CurrencyFormatter.format(
                nextPaymentAmountValue,
                language
            )

        bindBasicInformation(
            holder = holder,
            loan = loan,
            formattedAmount = formattedAmount
        )

        bindPaymentInformation(
            holder = holder,
            loan = loan,
            formattedTotalRepayment = formattedTotalRepayment,
            formattedPaymentAmount = formattedPaymentAmount,
            formattedRemainingBalance = formattedRemainingBalance
        )

        bindProgress(
            holder = holder,
            totalRepayment = totalRepaymentValue,
            remainingBalance = remainingBalanceValue
        )

        bindAutoPay(
            holder = holder,
            loan = loan,
            formattedNextPaymentAmount = formattedNextPaymentAmount
        )
    }

    private fun bindBasicInformation(
        holder: LoanViewHolder,
        loan: LoanModel,
        formattedAmount: String
    ) {
        holder.amount.text =
            if (language == "fr") {
                "Montant : $formattedAmount"
            } else {
                "Amount: $formattedAmount"
            }

        holder.reason.text =
            if (language == "fr") {
                "Raison : ${loan.reason}"
            } else {
                "Reason: ${loan.reason}"
            }

        holder.status.text =
            if (language == "fr") {
                "Statut : ${translateStatus(loan.status)}"
            } else {
                "Status: ${formatEnglishStatus(loan.status)}"
            }
    }

    private fun bindPaymentInformation(
        holder: LoanViewHolder,
        loan: LoanModel,
        formattedTotalRepayment: String,
        formattedPaymentAmount: String,
        formattedRemainingBalance: String
    ) {
        val planText =
            getPlanText(loan.paymentFrequency)

        val formattedDueDate =
            formatDate(
                timestamp = loan.dueDate,
                unavailableText = "N/A"
            )

        if (language == "fr") {
            holder.plan.text =
                "Plan : $planText"

            holder.interest.text =
                "Intérêt : ${loan.interestRate}%"

            holder.totalRepayment.text =
                "Remboursement total : $formattedTotalRepayment"

            holder.paymentAmount.text =
                "Montant du paiement : $formattedPaymentAmount"

            holder.remainingBalance.text =
                "Solde restant : $formattedRemainingBalance"

            holder.dueDate.text =
                "Date d’échéance : $formattedDueDate"
        } else {
            holder.plan.text =
                "Plan: $planText"

            holder.interest.text =
                "Interest: ${loan.interestRate}%"

            holder.totalRepayment.text =
                "Total Repayment: $formattedTotalRepayment"

            holder.paymentAmount.text =
                "Payment Amount: $formattedPaymentAmount"

            holder.remainingBalance.text =
                "Remaining Balance: $formattedRemainingBalance"

            holder.dueDate.text =
                "Due Date: $formattedDueDate"
        }
    }

    private fun bindProgress(
        holder: LoanViewHolder,
        totalRepayment: Double,
        remainingBalance: Double
    ) {
        val paidAmount =
            (totalRepayment - remainingBalance)
                .coerceAtLeast(0.0)

        val progressPercent =
            if (totalRepayment > 0.0) {
                (
                        paidAmount /
                                totalRepayment *
                                100
                        )
                    .toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }

        holder.progressBar.progress =
            progressPercent

        holder.progressText.text =
            if (language == "fr") {
                "Progression : $progressPercent%"
            } else {
                "Progress: $progressPercent%"
            }
    }

    private fun bindAutoPay(
        holder: LoanViewHolder,
        loan: LoanModel,
        formattedNextPaymentAmount: String
    ) {
        if (!loan.autoPayEnabled) {
            holder.autoPayStatus.text =
                if (language == "fr") {
                    "Paiement automatique : Désactivé"
                } else {
                    "Auto Pay: Disabled"
                }

            holder.nextPayment.text = ""
            return
        }

        val nextDateText =
            if (loan.nextPaymentDate > 0L) {
                formatDate(
                    timestamp = loan.nextPaymentDate,
                    unavailableText =
                        if (language == "fr") {
                            "En attente"
                        } else {
                            "Pending"
                        }
                )
            } else {
                if (language == "fr") {
                    "En attente d’approbation"
                } else {
                    "Pending approval"
                }
            }

        if (language == "fr") {
            holder.autoPayStatus.text =
                "Paiement automatique : ${
                    translateAutoPayStatus(
                        loan.autoPayStatus
                    )
                }"

            holder.nextPayment.text =
                "Prochain paiement : " +
                        "$formattedNextPaymentAmount " +
                        "le $nextDateText"
        } else {
            holder.autoPayStatus.text =
                "Auto Pay: ${
                    formatEnglishStatus(
                        loan.autoPayStatus
                    )
                }"

            holder.nextPayment.text =
                "Next Payment: " +
                        "$formattedNextPaymentAmount " +
                        "on $nextDateText"
        }
    }

    private fun getPlanText(
        paymentFrequency: String
    ): String {
        return if (language == "fr") {
            when (paymentFrequency.lowercase()) {
                "one_time" -> "Paiement unique"
                "monthly" -> "Mensuel"
                "weekly" -> "Hebdomadaire"
                else -> paymentFrequency
            }
        } else {
            when (paymentFrequency.lowercase()) {
                "one_time" -> "One-Time Payment"
                "monthly" -> "Monthly"
                "weekly" -> "Weekly"
                else -> formatEnglishStatus(paymentFrequency)
            }
        }
    }

    private fun formatDate(
        timestamp: Long,
        unavailableText: String
    ): String {
        if (timestamp <= 0L) {
            return unavailableText
        }

        val locale =
            if (language == "fr") {
                Locale.FRANCE
            } else {
                Locale.US
            }

        return SimpleDateFormat(
            "MMM dd, yyyy",
            locale
        ).format(Date(timestamp))
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

    private fun translateStatus(
        status: String
    ): String {
        return when (status.lowercase()) {
            "pending" -> "En attente"
            "approved" -> "Approuvé"
            "rejected" -> "Rejeté"
            "overdue" -> "En retard"
            "paid" -> "Payé"
            "scheduled" -> "Planifié"
            "completed" -> "Terminé"
            "disabled" -> "Désactivé"
            else -> status
        }
    }

    private fun translateAutoPayStatus(
        status: String
    ): String {
        return when (status.lowercase()) {
            "scheduled" -> "Planifié"
            "completed" -> "Terminé"
            "disabled" -> "Désactivé"
            "pending" -> "En attente"
            else -> status
        }
    }

    private fun formatEnglishStatus(
        value: String
    ): String {
        return value
            .replace("_", " ")
            .replaceFirstChar { character ->
                character.uppercase()
            }
    }

    override fun getItemCount(): Int {
        return loanList.size
    }

    fun updateLanguage(
        newLanguage: String
    ) {
        language = newLanguage
        notifyDataSetChanged()
    }
}