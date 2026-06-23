package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUserLoanAdapter(
    private val loanList: List<Pair<String, AdminLoanModel>>
) : RecyclerView.Adapter<AdminUserLoanAdapter.UserLoanViewHolder>() {

    class UserLoanViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val amount: TextView =
            itemView.findViewById(R.id.txtLoanAmount)

        val reason: TextView =
            itemView.findViewById(R.id.txtLoanReason)

        val status: TextView =
            itemView.findViewById(R.id.txtLoanStatus)

        val balance: TextView =
            itemView.findViewById(R.id.txtLoanBalance)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserLoanViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_user_loan_item, parent, false)

        return UserLoanViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserLoanViewHolder,
        position: Int
    ) {

        val (_, loan) = loanList[position]

        holder.amount.text =
            "Amount: $${loan.amount}"

        holder.reason.text =
            "Reason: ${loan.reason}"

        holder.status.text =
            "Status: ${loan.status}"

        holder.balance.text =
            "Balance: $${loan.remainingBalance}"
    }

    override fun getItemCount() = loanList.size
}