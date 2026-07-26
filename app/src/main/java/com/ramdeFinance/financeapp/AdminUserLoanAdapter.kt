package com.ramdefinance.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUserLoanAdapter(
    private val loanList: List<Pair<String, AdminLoanModel>>,
    private var language: String = "en"
) : RecyclerView.Adapter<AdminUserLoanAdapter.UserLoanViewHolder>() {

    class UserLoanViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

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

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.admin_user_loan_item,
                parent,
                false
            )

        return UserLoanViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserLoanViewHolder,
        position: Int
    ) {
        val (_, loan) = loanList[position]


        val formattedAmount =
            CurrencyFormatter.format(
                amount = parseMoney(loan.amount),
                currencyCode = "XOF",
                languageCode = language
            )

        val formattedBalance =
            CurrencyFormatter.format(
                amount = parseMoney(loan.remainingBalance),
                currencyCode = "XOF",
                languageCode = language
            )
        if (language == "fr") {
            holder.amount.text =
                "Montant : $formattedAmount"

            holder.reason.text =
                "Raison : ${loan.reason}"

            holder.status.text =
                "Statut : ${translateStatus(loan.status)}"

            holder.balance.text =
                "Solde : $formattedBalance"
        } else {
            holder.amount.text =
                "Amount: $formattedAmount"

            holder.reason.text =
                "Reason: ${loan.reason}"

            holder.status.text =
                "Status: ${formatEnglishStatus(loan.status)}"

            holder.balance.text =
                "Balance: $formattedBalance"
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
                .replace("\u202F", "")
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

    private fun translateStatus(
        status: String
    ): String {
        return when (status.lowercase()) {
            "pending" -> "En attente"
            "approved" -> "Approuvé"
            "rejected" -> "Rejeté"
            "overdue" -> "En retard"
            "paid" -> "Payé"
            else -> status
        }
    }

    private fun formatEnglishStatus(
        status: String
    ): String {
        return status
            .replace("_", " ")
            .replaceFirstChar { character ->
                character.uppercase()
            }
    }

    fun updateLanguage(
        newLanguage: String
    ) {
        language = newLanguage
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return loanList.size
    }
}