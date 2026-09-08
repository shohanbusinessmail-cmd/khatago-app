package com.shohan.khatago.data

import androidx.room.withTransaction
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class FinanceRepository(private val database: FinanceDatabase) {
    private val dao = database.financeDao()

    val records: Flow<List<FinancialRecord>> = dao.observeRecords().combine(dao.observePayments()) { entities, paymentEntities ->
        val paidByRecord = paymentEntities.groupingBy { it.recordId }.fold(0L) { total, payment -> total + payment.amountMinor }
        entities.map { it.toDomain(paidByRecord[it.id] ?: 0L) }
    }
    val payments: Flow<List<Payment>> = dao.observePayments().map { list -> list.map { it.toDomain() } }

    suspend fun saveRecord(record: FinancialRecord) = dao.upsertRecord(record.toEntity())
    suspend fun deleteRecord(id: String) = dao.deleteRecord(id)
    suspend fun deletePayment(id: String) = dao.deletePayment(id)

    suspend fun editPayment(
        record: FinancialRecord,
        existing: Payment,
        amountMinor: Long,
        paidOnEpochDay: Int,
        method: String,
        reference: String,
        note: String
    ): Result<Payment> {
        if (amountMinor <= 0L) return Result.failure(IllegalArgumentException("Payment must be greater than zero."))
        val maximum = record.remainingMinor + existing.amountMinor
        if (amountMinor > maximum) return Result.failure(IllegalArgumentException("Payment cannot be greater than the remaining balance."))
        val updated = existing.copy(amountMinor = amountMinor, paidOnEpochDay = paidOnEpochDay, method = method, reference = reference, note = note)
        dao.insertPayment(updated.toEntity())
        return Result.success(updated)
    }

    suspend fun addPayment(
        record: FinancialRecord,
        amountMinor: Long,
        paidOnEpochDay: Int,
        method: String,
        reference: String,
        note: String
    ): Result<Payment> {
        if (!record.type.isObligation) return Result.failure(IllegalArgumentException("This record does not accept payments."))
        if (amountMinor <= 0L) return Result.failure(IllegalArgumentException("Enter an amount greater than zero."))
        if (amountMinor > record.remainingMinor) return Result.failure(IllegalArgumentException("Payment cannot be greater than the remaining balance."))
        val payment = Payment(UUID.randomUUID().toString(), record.id, amountMinor, paidOnEpochDay, method, reference, note, System.currentTimeMillis())
        dao.insertPayment(payment.toEntity())
        return Result.success(payment)
    }

    suspend fun replaceAll(records: List<FinancialRecord>, payments: List<Payment>) {
        database.withTransaction { dao.replaceAll(records.map { it.toEntity() }, payments.map { it.toEntity() }) }
    }

    suspend fun clearAll() = database.withTransaction { dao.deleteAllPayments(); dao.deleteAllRecords() }
}
