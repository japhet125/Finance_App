package com.ramdefinance.financeapp

data class AuditLogModel(
    val actorId: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val message: String = "",
    val timestamp: Long = 0
)