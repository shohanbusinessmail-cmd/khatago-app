package com.shohan.khatago.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM financial_records ORDER BY dateEpochDay DESC, createdAt DESC")
    fun observeRecords(): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM payments ORDER BY paidOnEpochDay DESC, createdAt DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: FinancialRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecords(records: List<FinancialRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM financial_records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM financial_records WHERE id = :id")
    suspend fun deleteRecord(id: String)

    @Query("SELECT * FROM financial_records WHERE id = :id LIMIT 1")
    suspend fun getRecord(id: String): FinancialRecordEntity?

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM payments WHERE recordId = :recordId")
    suspend fun totalPaidFor(recordId: String): Long

    @Query("SELECT * FROM financial_records")
    suspend fun getAllRecords(): List<FinancialRecordEntity>

    @Query("SELECT * FROM payments")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Transaction
    suspend fun replaceAll(records: List<FinancialRecordEntity>, payments: List<PaymentEntity>) {
        deleteAllPayments()
        deleteAllRecords()
        upsertRecords(records)
        insertPayments(payments)
    }
}
