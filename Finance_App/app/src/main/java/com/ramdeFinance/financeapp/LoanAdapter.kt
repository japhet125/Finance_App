package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ProgressBar

class LoanAdapter(
    private val loanList: List<LoanModel>
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amount: TextView = itemView.findViewById(R.id.txtAmount)
        val reason: TextView = itemView.findViewById(R.id.txtReason)
        val status: TextView = itemView.findViewById(R.id.txtStatus)

        val plan: TextView = itemView.findViewById(R.id.txtPlan)
        val interest: TextView = itemView.findViewById(R.id.txtInterest)
        val totalRepayment: TextView = itemView.findViewById(R.id.txtTotalRepayment)
        val paymentAmount: TextView = itemView.findViewById(R.id.txtPaymentAmount)
        val remainingBalance: TextView = itemView.findViewById(R.id.txtRemainingBalance)
        val dueDate: TextView = itemView.findViewById(R.id.txtDueDate)
        val progressText: TextView = itemView.findViewById(R.id.txtLoanProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressLoan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.loan_item, parent, false)

        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {

        val loan = loanList[position]

        holder.amount.text = "Amount: $${loan.amount}"
        holder.reason.text = "Reason: ${loan.reason}"
        holder.status.text = "Status: ${loan.status}"


        holder.plan.text = "Plan: ${loan.paymentFrequency} for ${loan.paymentTerm} payments"
        holder.interest.text = "Interest: ${loan.interestRate}%"
        holder.totalRepayment.text = "Total Repayment: $${loan.totalRepayment}"
        holder.paymentAmount.text = "Payment Amount: $${loan.paymentAmount}"
        holder.remainingBalance.text = "Remaining Balance: $${loan.remainingBalance}"

        val formattedDueDate = if (loan.dueDate > 0) {
            SimpleDateFormat(
                "MMM dd, yyyy",
                Locale.getDefault()
            ).format(Date(loan.dueDate))
        } else {
            "N/A"
        }

        holder.dueDate.text = "Due Date: $formattedDueDate"

        val totalRepayment = loan.totalRepayment.toDoubleOrNull() ?: 0.0
        val remainingBalance = loan.remainingBalance.toDoubleOrNull() ?: 0.0

        val paidAmount = totalRepayment - remainingBalance

        val progressPercent = if (totalRepayment > 0) {
            ((paidAmount / totalRepayment) * 100).toInt()
        } else {
            0
        }

        holder.progressText.text = "Progress: $progressPercent%"
        holder.progressBar.progress = progressPercent
    }

    override fun getItemCount(): Int {
        return loanList.size
    }
}