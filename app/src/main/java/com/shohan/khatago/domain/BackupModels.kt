package com.shohan.khatago.domain

data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: Long,
    val settings: AppSettings,
    val records: List<FinancialRecord>,
    val payments: List<Payment>
)

class BackupValidationException(message: String) : Exception(message)
