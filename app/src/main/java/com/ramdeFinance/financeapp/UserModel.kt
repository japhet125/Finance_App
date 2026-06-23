package com.ramdefinance.financeapp

data class UserModel(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "user",
    val creditScore: Long = 500
)