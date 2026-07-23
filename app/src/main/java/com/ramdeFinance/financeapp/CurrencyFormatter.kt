package com.ramdefinance.financeapp

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyFormatter {

    fun format(
        amount: Double,
        currencyCode: String,
        languageCode: String
    ): String {
        return when (currencyCode.uppercase()) {
            "XOF" -> formatXof(amount, languageCode)
            "USD" -> formatUsd(amount, languageCode)
            else -> "$amount $currencyCode"
        }
    }

    private fun formatXof(
        amount: Double,
        languageCode: String
    ): String {
        val locale = if (languageCode == "fr") {
            Locale.FRANCE
        } else {
            Locale.US
        }

        val number = NumberFormat
            .getNumberInstance(locale)
            .format(amount.roundToLong())

        return "$number F CFA"
    }

    private fun formatUsd(
        amount: Double,
        languageCode: String
    ): String {
        val locale = if (languageCode == "fr") {
            Locale.FRANCE
        } else {
            Locale.US
        }

        return NumberFormat
            .getCurrencyInstance(locale)
            .apply {
                currency = Currency.getInstance("USD")
            }
            .format(amount)
    }
}