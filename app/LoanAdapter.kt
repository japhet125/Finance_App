package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LoanAdapter(
    private val loanList: List<LoanModel>
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amount: TextView = itemView.findViewById(R.id.txtAmount)
        val reason: TextView = itemView.findViewById(R.id.txtReason)
        val status: TextView = itemView.findViewById(R.id.txtStatus)
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
    }

    override fun getItemCount(): Int {
        return loanList.size
    }
}