package com.ramdefinance.financeapp

data class TransactionModel(
    val userId: String = "",
    val loanId: String = "",
    val paymentAmount: String = "",
    val previousBalance: String = "",
    val newBalance: String = "",
    val paymentDate: Long = 0,
    val paymentType: String = ""
)