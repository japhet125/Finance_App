package com.ramdefinance.financeapp

data class IdentityUserModel(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val apt: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",
    val language: String = "en",
    val identityStatus: String = "",
    val identityVerified: Boolean = false,
    val identityDocumentUrl: String = ""
)