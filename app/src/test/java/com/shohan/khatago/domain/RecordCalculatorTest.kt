package com.shohan.khatago.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RecordCalculatorTest {
    private val today = LocalDate.of(2025, 1, 10)
    private fun record(type: RecordType = RecordType.SHOP_CREDIT, due: LocalDate? = today.plusDays(5), amount: Long = 10000, paid: Long = 0) = FinancialRecord("id", type, "Rice", "Market", "Food", amount, today.toEpochDay().toInt(), due?.toEpochDay()?.toInt(), null, null, "", null, null, paid)

    @Test fun partialPaymentIsNotPaid() { assertEquals(RecordStatus.PARTIALLY_PAID, RecordCalculator.status(record(paid = 2500), today)) }
    @Test fun dueLabelsCoverImportantBoundaries() {
        assertEquals(RecordStatus.DUE_TODAY, RecordCalculator.status(record(due = today), today))
        assertEquals(RecordStatus.OVERDUE, RecordCalculator.status(record(due = today.minusDays(1)), today))
        assertEquals(RecordStatus.DUE_SOON, RecordCalculator.status(record(due = today.plusDays(7)), today))
        assertEquals("2 days overdue", RecordCalculator.dueLabel(record(due = today.minusDays(2)), today))
    }
    @Test fun summarySeparatesMoneyOwedAndMoneyLent() {
        val summary = RecordCalculator.calculateSummary(listOf(record(RecordType.SHOP_CREDIT, amount = 10000), record(RecordType.LENT, amount = 5000)), today)
        assertEquals(10000L, summary.outstandingMinor)
        assertEquals(5000L, summary.owedToMeMinor)
    }
    @Test fun settledRecordsCannotBecomeNegative() { assertEquals(RecordStatus.PAID, RecordCalculator.status(record(paid = 15000), today)) }
}
