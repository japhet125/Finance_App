package com.ramdefinance.financeapp

data class LoanModel(
    val amount: String = "",
    val reason: String = "",
    val status: String = "",
    val principalAmount: String = "",
    val interestRate: Long = 0,
    val totalRepayment: String = "",
    val paymentFrequency: String = "",
    val paymentTerm: Long = 0,
    val paymentAmount: String = "",
    val remainingBalance: String = "",
    val dueDate: Long = 0,
    val overduePenaltyApplied: Boolean = false,
    val createdAt: Long = 0
)