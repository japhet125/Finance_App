package com.ramdefinance.financeapp

data class AdminLoanModel(
    val userId: String = "",
    val amount: String = "",
    val reason: String = "",
    val paymentFrequency: String = "",
    val paymentTerm: Long = 0,
    val paymentAmount: String = "",
    val autoPayEnabled: Boolean = false,
    val autoPayStatus: String = "",
    val nextPaymentDate: Long = 0,
    val nextPaymentAmount: String = "",
    val status: String = "",
    val remainingBalance: String = ""
)