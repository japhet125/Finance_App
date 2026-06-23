package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactionList: List<TransactionModel>,
    private var language: String = "en"
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val amount: TextView =
            itemView.findViewById(R.id.txtTransactionAmount)

        val previousBalance: TextView =
            itemView.findViewById(R.id.txtPreviousBalance)

        val newBalance: TextView =
            itemView.findViewById(R.id.txtNewBalance)

        val paymentDate: TextView =
            itemView.findViewById(R.id.txtPaymentDate)
    }

    fun updateLanguage(newLanguage: String) {
        language = newLanguage
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val transaction = transactionList[position]

        val formattedDate = SimpleDateFormat(
            "MMM dd, yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(transaction.paymentDate))

        if (language == "fr") {
            holder.amount.text =
                "Paiement : ${transaction.paymentAmount}"

            holder.previousBalance.text =
                "Solde précédent : ${transaction.previousBalance}"

            holder.newBalance.text =
                "Nouveau solde : ${transaction.newBalance}"

            holder.paymentDate.text =
                "Date : $formattedDate"
        } else {
            holder.amount.text =
                "Payment: $${transaction.paymentAmount}"

            holder.previousBalance.text =
                "Previous Balance: $${transaction.previousBalance}"

            holder.newBalance.text =
                "New Balance: $${transaction.newBalance}"

            holder.paymentDate.text =
                "Date: $formattedDate"
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}