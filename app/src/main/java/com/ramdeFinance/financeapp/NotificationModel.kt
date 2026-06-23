package com.ramdefinance.financeapp

data class NotificationModel(
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)