package com.ramdefinance.financeapp

data class AdminPaymentModel(
    val userId: String = "",
    val loanId: String = "",
    val paymentAmount: String = "",
    val previousBalance: String = "",
    val newBalance: String = "",
    val paymentDate: Long = 0L,
    val paymentType: String = "",
    val status: String = ""
)