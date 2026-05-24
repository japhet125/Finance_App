package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoanAdapter(
    private val loanList: List<Pair<String, AdminLoanModel>>
) : RecyclerView.Adapter<AdminLoanAdapter.AdminLoanViewHolder>() {

    class AdminLoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amount: TextView = itemView.findViewById(R.id.txtAdminAmount)
        val reason: TextView = itemView.findViewById(R.id.txtAdminReason)
        val status: TextView = itemView.findViewById(R.id.txtAdminStatus)

        val approveButton: Button = itemView.findViewById(R.id.btnApprove)
        val rejectButton: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminLoanViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_loan_item, parent, false)

        return AdminLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminLoanViewHolder, position: Int) {

        val (documentId, loan) = loanList[position]

        holder.amount.text = "Amount: $${loan.amount}"
        holder.reason.text = "Reason: ${loan.reason}"
        holder.status.text = "Status: ${loan.status}"

        if (loan.status == "approved") {

            holder.approveButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE

            holder.status.text = "✓ Approved"

        } else if (loan.status == "rejected") {

            holder.approveButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE

            holder.status.text = "✗ Rejected"

        } else {

            holder.approveButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE

            holder.status.text = "Status: Pending"
        }

        val db = FirebaseFirestore.getInstance()

        holder.approveButton.setOnClickListener {

            db.collection("loan_requests")
                .document(documentId)
            db.collection("loan_requests")
                .document(documentId)
                .update("status", "approved")
        }

        holder.rejectButton.setOnClickListener {

            db.collection("loan_requests")
                .document(documentId)
                .update("status", "rejected")
        }

    }

    override fun getItemCount(): Int {
        return loanList.size
    }
    private fun calculateMonthlyPayment(amount: String): String {

        val loanAmount = amount.toDoubleOrNull() ?: 0.0

        val payment = loanAmount / 12

        return String.format("%.2f", payment)
    }

    private fun calculateDueDate(): Long {

        val oneYearMillis = 365L * 24L * 60L * 60L * 1000L

        return System.currentTimeMillis() + oneYearMillis
    }
}