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

        val locale =
            if (language == "fr") {
                Locale.FRANCE
            } else {
                Locale.US
            }

        val formattedDate = SimpleDateFormat(
            "MMM dd, yyyy HH:mm",
            locale
        ).format(Date(transaction.paymentDate))

        val formattedPaymentAmount =
            CurrencyFormatter.format(
                amount = parseMoney(transaction.paymentAmount),
                currencyCode = "XOF",
                languageCode = language
            )

        val formattedPreviousBalance =
            CurrencyFormatter.format(
                amount = parseMoney(transaction.previousBalance),
                currencyCode = "XOF",
                languageCode = language
            )

        val formattedNewBalance =
            CurrencyFormatter.format(
                amount = parseMoney(transaction.newBalance),
                currencyCode = "XOF",
                languageCode = language
            )

        if (language == "fr") {
            holder.amount.text =
                "Paiement : $formattedPaymentAmount"

            holder.previousBalance.text =
                "Solde précédent : $formattedPreviousBalance"

            holder.newBalance.text =
                "Nouveau solde : $formattedNewBalance"

            holder.paymentDate.text =
                "Date : $formattedDate"
        } else {
            holder.amount.text =
                "Payment: $formattedPaymentAmount"

            holder.previousBalance.text =
                "Previous Balance: $formattedPreviousBalance"

            holder.newBalance.text =
                "New Balance: $formattedNewBalance"

            holder.paymentDate.text =
                "Date: $formattedDate"
        }
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
                cleanedValue
                    .toDoubleOrNull()
                    ?: 0.0
            }
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}