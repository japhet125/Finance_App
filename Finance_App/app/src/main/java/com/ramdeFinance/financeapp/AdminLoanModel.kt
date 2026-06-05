package com.ramdefinance.financeapp

data class AdminLoanModel(
    val userId: String = "",
    val amount: String = "",
    val reason: String = "",
    val paymentFrequency: String = "",
    val paymentTerm: Long = 0,
    val status: String = "",
    val remainingBalance: String = ""
)