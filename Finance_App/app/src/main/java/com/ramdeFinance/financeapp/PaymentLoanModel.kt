package com.ramdefinance.financeapp

data class PaymentLoanModel(
    val amount: String = "",
    val reason: String = "",
    val status: String = "",
    val paymentFrequency: String = "",
    val paymentTerm: Long = 0,
    val paymentAmount: String = "",
    val remainingBalance: String = "",
    val totalRepayment: String = ""
)