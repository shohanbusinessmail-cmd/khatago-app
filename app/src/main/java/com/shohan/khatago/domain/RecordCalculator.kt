package com.shohan.khatago.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Pure, deterministic rules for balances and due labels. Kept outside UI for testing. */
object RecordCalculator {
    fun status(record: FinancialRecord, today: LocalDate = LocalDate.now()): RecordStatus {
        if (!record.type.isObligation) return RecordStatus.COMPLETED
        if (record.remainingMinor <= 0L) return RecordStatus.PAID
        val due = record.dueDateEpochDay?.let { LocalDate.ofEpochDay(it.toLong()) } ?: return if (record.paidMinor > 0) RecordStatus.PARTIALLY_PAID else RecordStatus.ACTIVE
        val days = ChronoUnit.DAYS.between(today, due)
        return when {
            days < 0 -> RecordStatus.OVERDUE
            days == 0L -> RecordStatus.DUE_TODAY
            days <= 7L -> RecordStatus.DUE_SOON
            record.paidMinor > 0L -> RecordStatus.PARTIALLY_PAID
            else -> RecordStatus.UPCOMING
        }
    }

    fun dueLabel(record: FinancialRecord, today: LocalDate = LocalDate.now()): String {
        val due = record.dueDateEpochDay?.let { LocalDate.ofEpochDay(it.toLong()) } ?: return "No due date"
        val days = ChronoUnit.DAYS.between(today, due)
        return when {
            days < 0 -> "${-days} ${if (days == -1L) "day" else "days"} overdue"
            days == 0L -> "Due today"
            days == 1L -> "Due tomorrow"
            else -> "Due in $days days"
        }
    }

    fun calculateSummary(records: List<FinancialRecord>, today: LocalDate = LocalDate.now()): DashboardSummary {
        var outstanding = 0L
        var owedToMe = 0L
        var paid = 0L
        var income = 0L
        var expense = 0L
        var dueSoon = 0L
        var overdue = 0L
        var dueSoonCount = 0
        var overdueCount = 0
        records.forEach { record ->
            when (record.type) {
                RecordType.INCOME -> income += record.amountMinor
                RecordType.EXPENSE -> expense += record.amountMinor
                RecordType.LENT -> {
                    owedToMe += record.remainingMinor
                    paid += record.paidMinor
                }
                else -> if (record.type.isObligation) {
                    outstanding += record.remainingMinor
                    paid += record.paidMinor
                    when (status(record, today)) {
                        RecordStatus.OVERDUE -> { overdue += record.remainingMinor; overdueCount++ }
                        RecordStatus.DUE_TODAY, RecordStatus.DUE_SOON -> { dueSoon += record.remainingMinor; dueSoonCount++ }
                        else -> Unit
                    }
                }
            }
        }
        return DashboardSummary(outstanding, owedToMe, paid, income, expense, dueSoon, overdue, dueSoonCount, overdueCount)
    }
}
