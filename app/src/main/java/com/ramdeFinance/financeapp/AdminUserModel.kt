package com.ramdefinance.financeapp

data class AdminUserModel(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val creditScore: Long = 500,
    val identityStatus: String = "",
    val identityVerified: Boolean = false
)