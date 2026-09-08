package com.shohan.khatago.data

import com.shohan.khatago.domain.AppSettings
import com.shohan.khatago.domain.BackupSnapshot
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BackupCodecTest {
    @Test fun roundTripPreservesRecordsAndPayments() {
        val record = FinancialRecord("r1", RecordType.BORROWED, "Emergency cash", "A friend", "", 250000, LocalDate.now().toEpochDay().toInt(), null, null, null, "", null, null)
        val payment = Payment("p1", "r1", 50000, LocalDate.now().toEpochDay().toInt(), "Cash", "", "", 1L)
        val decoded = try {
            BackupCodec.decode(BackupCodec.encode(BackupSnapshot(1, 2L, AppSettings("Shohan", "BDT", true, true), listOf(record), listOf(payment))))
        } catch (error: Exception) {
            System.err.println("BACKUP-ERROR:${error::class.java.name}:${error.message}")
            throw AssertionError("Backup decode failed: ${error::class.java.name}: ${error.message}", error)
        }
        assertEquals("Shohan", decoded.settings.name)
        assertEquals(1, decoded.records.size)
        assertEquals(50000L, decoded.payments.single().amountMinor)
    }
    @Test(expected = com.shohan.khatago.domain.BackupValidationException::class)
    fun rejectsUnknownFormat() { BackupCodec.decode("{\"format\":\"not-khatago\"}") }
}
