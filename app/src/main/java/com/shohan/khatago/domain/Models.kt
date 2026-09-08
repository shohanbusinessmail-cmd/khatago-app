package com.shohan.khatago.domain

/** The eight financial record families supported by KhataGo. */
enum class RecordType(val label: String, val description: String, val isObligation: Boolean) {
    SHOP_CREDIT("Shop credit", "Track purchases you will pay for later", true),
    LOAN("Loan", "Track a bank or NGO repayment plan", true),
    EMI("EMI purchase", "Track a product paid in installments", true),
    BORROWED("Money borrowed", "Track money you owe to someone", true),
    LENT("Money lent", "Track money others owe you", true),
    INCOME("Income", "Record money coming in", false),
    EXPENSE("Expense", "Record money going out", false);

    companion object {
        fun from(value: String): RecordType = entries.firstOrNull { it.name == value } ?: EXPENSE
    }
}

enum class RecordStatus(val label: String) {
    ACTIVE("Active"),
    UPCOMING("Upcoming"),
    DUE_TODAY("Due today"),
    DUE_SOON("Due soon"),
    PARTIALLY_PAID("Partially paid"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    COMPLETED("Recorded")
}

data class FinancialRecord(
    val id: String,
    val type: RecordType,
    val title: String,
    val counterparty: String,
    val category: String,
    val amountMinor: Long,
    val dateEpochDay: Int,
    val dueDateEpochDay: Int?,
    val installmentAmountMinor: Long?,
    val installmentCount: Int?,
    val notes: String,
    val paymentMethod: String?,
    val phone: String?,
    val paidMinor: Long = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val remainingMinor: Long get() = MoneyMath.remaining(amountMinor, paidMinor)
    val progress: Float get() = MoneyMath.progress(amountMinor, paidMinor)
    val isSettled: Boolean get() = !type.isObligation || remainingMinor == 0L
    val paymentsCountedAsInstallments: Int
        get() = installmentAmountMinor?.takeIf { it > 0 }?.let { (paidMinor / it).toInt() }?.coerceAtMost(installmentCount ?: Int.MAX_VALUE) ?: 0
}

data class Payment(
    val id: String,
    val recordId: String,
    val amountMinor: Long,
    val paidOnEpochDay: Int,
    val method: String,
    val reference: String,
    val note: String,
    val createdAt: Long
)

data class CurrencyOption(val code: String, val symbol: String, val name: String) {
    companion object {
        val supported = listOf(
            CurrencyOption("BDT", "৳", "Bangladeshi Taka"),
            CurrencyOption("USD", "$", "US Dollar"),
            CurrencyOption("EUR", "€", "Euro"),
            CurrencyOption("GBP", "£", "British Pound"),
            CurrencyOption("INR", "₹", "Indian Rupee"),
            CurrencyOption("SAR", "﷼", "Saudi Riyal"),
            CurrencyOption("AED", "د.إ", "UAE Dirham")
        )
        fun find(code: String): CurrencyOption = supported.firstOrNull { it.code == code } ?: supported.first()
    }
}

data class DashboardSummary(
    val outstandingMinor: Long = 0,
    val owedToMeMinor: Long = 0,
    val paidMinor: Long = 0,
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
    val dueSoonMinor: Long = 0,
    val overdueMinor: Long = 0,
    val dueSoonCount: Int = 0,
    val overdueCount: Int = 0
) {
    val netFlowMinor: Long get() = incomeMinor - expenseMinor
}

data class AppSettings(
    val name: String = "",
    val currencyCode: String = "BDT",
    val onboardingComplete: Boolean = false,
    val notificationsEnabled: Boolean = true
)

object MoneyMath {
    fun remaining(amountMinor: Long, paidMinor: Long): Long = (amountMinor - paidMinor.coerceAtLeast(0L)).coerceAtLeast(0L)
    fun progress(amountMinor: Long, paidMinor: Long): Float = if (amountMinor <= 0) 0f else (paidMinor.toDouble() / amountMinor).coerceIn(0.0, 1.0).toFloat()
    fun validateAmount(value: String): Long? {
        val normalized = value.trim().replace(",", "")
        if (normalized.isBlank()) return null
        return try {
            val parts = normalized.split('.')
            if (parts.size > 2 || parts.any { it.isBlank() && parts.size == 1 }) return null
            val whole = parts[0].toLong()
            if (whole < 0) return null
            val decimals = (parts.getOrNull(1) ?: "").padEnd(2, '0').take(2)
            if (parts.getOrNull(1)?.length ?: 0 > 2) return null
            whole * 100 + (decimals.toLongOrNull() ?: 0)
        } catch (_: NumberFormatException) {
            null
        }
    }
}
