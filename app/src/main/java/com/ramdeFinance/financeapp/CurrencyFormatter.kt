package com.ramdefinance.financeapp

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    fun format(
        amount: Double,
        languageCode: String
    ): String {
        return if (languageCode == "fr") {
            formatCfa(amount)
        } else {
            formatUsd(amount)
        }
    }

    private fun formatUsd(amount: Double): String {
        val formatter =
            NumberFormat.getCurrencyInstance(Locale.US)

        formatter.currency = Currency.getInstance("USD")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        return formatter.format(amount)
    }

    private fun formatCfa(amount: Double): String {
        val formatter =
            NumberFormat.getNumberInstance(Locale.FRANCE)

        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        return "${formatter.format(amount)} F CFA"
    }
}