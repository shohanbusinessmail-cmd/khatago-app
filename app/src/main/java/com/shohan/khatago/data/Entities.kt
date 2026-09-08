package com.shohan.khatago.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shohan.khatago.domain.FinancialRecord
import com.shohan.khatago.domain.Payment
import com.shohan.khatago.domain.RecordType

@Entity(
    tableName = "financial_records",
    indices = [Index("type"), Index("dateEpochDay"), Index("dueDateEpochDay"), Index("title"), Index("counterparty")]
)
data class FinancialRecordEntity(
    @PrimaryKey val id: String,
    val type: String,
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
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(
        entity = FinancialRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recordId"), Index("paidOnEpochDay")]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val recordId: String,
    val amountMinor: Long,
    val paidOnEpochDay: Int,
    val method: String,
    val reference: String,
    val note: String,
    val createdAt: Long
)

fun FinancialRecordEntity.toDomain(paidMinor: Long = 0L) = FinancialRecord(
    id = id, type = RecordType.from(type), title = title, counterparty = counterparty,
    category = category, amountMinor = amountMinor, dateEpochDay = dateEpochDay,
    dueDateEpochDay = dueDateEpochDay, installmentAmountMinor = installmentAmountMinor,
    installmentCount = installmentCount, notes = notes, paymentMethod = paymentMethod,
    phone = phone, paidMinor = paidMinor, createdAt = createdAt, updatedAt = updatedAt
)

fun FinancialRecord.toEntity(now: Long = System.currentTimeMillis()) = FinancialRecordEntity(
    id = id, type = type.name, title = title, counterparty = counterparty, category = category,
    amountMinor = amountMinor, dateEpochDay = dateEpochDay, dueDateEpochDay = dueDateEpochDay,
    installmentAmountMinor = installmentAmountMinor, installmentCount = installmentCount,
    notes = notes, paymentMethod = paymentMethod, phone = phone,
    createdAt = if (createdAt == 0L) now else createdAt, updatedAt = now
)

fun PaymentEntity.toDomain() = Payment(id, recordId, amountMinor, paidOnEpochDay, method, reference, note, createdAt)
fun Payment.toEntity() = PaymentEntity(id, recordId, amountMinor, paidOnEpochDay, method, reference, note, createdAt)
