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
        val recommendation: TextView =
            itemView.findViewById(R.id.txtRecommendation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminLoanViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_loan_item, parent, false)

        return AdminLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminLoanViewHolder, position: Int) {

        val (documentId, loan) = loanList[position]
        val db = FirebaseFirestore.getInstance()
        holder.recommendation.text = "Recommendation: Loading..."


        holder.amount.text = "Amount: $${loan.amount}"
        holder.reason.text = "Reason: ${loan.reason}"
        holder.status.text = "Status: ${loan.status}"

        db.collection("users")
            .document(loan.userId)
            .get()
            .addOnSuccessListener { userDocument ->

                val creditScore =
                    userDocument.getLong("creditScore") ?: 500

                val recommendationText = when {
                    creditScore >= 700 -> {
                        "✅ Recommendation: Approve — strong credit score"
                    }

                    creditScore >= 550 -> {
                        "⚠️ Recommendation: Review — medium risk borrower"
                    }

                    else -> {
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
}